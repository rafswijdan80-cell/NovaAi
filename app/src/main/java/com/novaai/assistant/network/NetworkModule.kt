package com.novaai.assistant.network

import com.novaai.assistant.ai.AiProviderManager

object NetworkModule {
    const val CONNECT_TIMEOUT_MS = 15_000
    const val READ_TIMEOUT_MS = 30_000
    const val TOTAL_TIMEOUT_MS = 35_000L

    /** Daftar provider. Tambahkan provider baru di sini. */
    fun createProviderManager(): AiProviderManager {
        return AiProviderManager(
            listOf(
                OfficialAiProvider(),
                GeminiAdditionalProvider()
            )
        )
    }
}
