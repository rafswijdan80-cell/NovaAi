package com.novaai.assistant.ai

import com.novaai.assistant.network.NetworkModule
import kotlinx.coroutines.withTimeoutOrNull

class AiRouter(private val manager: AiProviderManager) {

    suspend fun send(request: AiRequest, config: RoutingConfig): AiResult {
        if (config.mode == SelectionMode.MANUAL) {
            return attempt(config.manualProvider, request, config)
        }

        // Mode otomatis: hanya provider utama dan cadangan yang dipilih pengguna.
        val candidates = listOfNotNull(config.primary, config.backup).distinct()
        var lastFailure: AiResult.Failure? = null

        for (id in candidates) {
            val result = attempt(id, request, config)
            if (result is AiResult.Success) return result
            lastFailure = result as? AiResult.Failure
        }

        return lastFailure ?: AiResult.Failure(AiError.NoActiveProvider)
    }

    /**
     * Satu-satunya tempat prompt dikirim ke provider.
     * Urutan pemeriksaan: aktif -> persetujuan (jika perlu) -> konfigurasi -> kirim.
     */
    private suspend fun attempt(
        id: ProviderId,
        request: AiRequest,
        config: RoutingConfig
    ): AiResult {
        val provider = manager.get(id)
            ?: return AiResult.Failure(AiError.UnknownProvider)

        if (id !in config.enabled) {
            return AiResult.Failure(AiError.Disabled(id))
        }
        if (id == ProviderId.GEMINI_TAMBAHAN && !config.geminiConsent) {
            return AiResult.Failure(AiError.ConsentRequired(id))
        }
        if (!provider.isConfigured()) {
            return AiResult.Failure(AiError.NotConfigured(id))
        }

        return withTimeoutOrNull(NetworkModule.TOTAL_TIMEOUT_MS) {
            provider.send(request)
        } ?: AiResult.Failure(AiError.Timeout)
    }
}
