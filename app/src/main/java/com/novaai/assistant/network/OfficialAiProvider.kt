package com.novaai.assistant.network

import com.novaai.assistant.ai.AiProvider
import com.novaai.assistant.ai.AiError
import com.novaai.assistant.ai.AiRequest
import com.novaai.assistant.ai.AiResult
import com.novaai.assistant.ai.ProviderId

/**
 * Placeholder resmi untuk provider utama.
 * Belum ada URL backend atau kredensial, sehingga provider ini TIDAK bisa dipakai.
 * Integrasi produksi harus melalui backend proxy; API key tidak boleh ada di APK.
 */
class OfficialAiProvider : AiProvider {
    override val id: ProviderId = ProviderId.OFFICIAL_PRIMARY

    override suspend fun isConfigured(): Boolean = false

    override suspend fun send(request: AiRequest): AiResult {
        return AiResult.Failure(AiError.NotConfigured(id))
    }
}
