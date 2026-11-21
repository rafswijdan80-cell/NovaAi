package com.novaai.assistant.network

import com.novaai.assistant.ai.AiError
import com.novaai.assistant.ai.AiProvider
import com.novaai.assistant.ai.AiReply
import com.novaai.assistant.ai.AiRequest
import com.novaai.assistant.ai.AiResult
import com.novaai.assistant.ai.ProviderId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.URLEncoder

/**
 * Gemini Tambahan.
 * PERINGATAN: endpoint memakai GET, sehingga isi pertanyaan dapat tercatat di URL atau log server.
 * Endpoint ini belum diverifikasi oleh NOVA AI.
 */
class GeminiAdditionalProvider : AiProvider {

    override val id: ProviderId = ProviderId.GEMINI_TAMBAHAN

    override suspend fun isConfigured(): Boolean = true

    override suspend fun send(request: AiRequest): AiResult = withContext(Dispatchers.IO) {
        try {
            val url = buildUrl(request.prompt, request.sessionId)
            val body = fetchBody(url)
            parseResponse(body)
        } catch (e: SocketTimeoutException) {
            AiResult.Failure(AiError.Timeout)
        } catch (e: HttpStatusException) {
            AiResult.Failure(AiError.Provider("Server Gemini Tambahan merespons kode ${e.code}."))
        } catch (e: IOException) {
            AiResult.Failure(AiError.NetworkUnavailable)
        }
    }

    private fun buildUrl(prompt: String, sessionId: String): String {
        return "$BASE_URL?text=${encode(prompt)}&sessionsId=${encode(sessionId)}"
    }

    private fun encode(value: String): String {
        return URLEncoder.encode(value, "UTF-8").replace("+", "%20")
    }

    private fun fetchBody(url: String): String {
        val connection = URL(url).openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = NetworkModule.CONNECT_TIMEOUT_MS
            connection.readTimeout = NetworkModule.READ_TIMEOUT_MS
            connection.useCaches = false
            connection.setRequestProperty("Accept", "application/json")

            val code = connection.responseCode
            if (code !in 200..299) {
                throw HttpStatusException(code)
            }
            return connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }

    private fun parseResponse(body: String): AiResult {
        return try {
            val json = JSONObject(body)
            if (!json.optBoolean("status", false)) {
                return AiResult.Failure(AiError.Provider("Gemini Tambahan mengembalikan status gagal."))
            }
            val result = json.optString("result", "").trim()
            if (result.isEmpty()) {
                return AiResult.Failure(AiError.InvalidResponse)
            }
            val remoteSessionId = json.optString("sessionId", "").ifBlank { null }
            AiResult.Success(AiReply(result, id, remoteSessionId))
        } catch (e: JSONException) {
            AiResult.Failure(AiError.InvalidResponse)
        }
    }

    private class HttpStatusException(val code: Int) : IOException("HTTP $code")

    private companion object {
        const val BASE_URL = "https://api.ikyyxd.my.id/ai/gemini"
    }
}
