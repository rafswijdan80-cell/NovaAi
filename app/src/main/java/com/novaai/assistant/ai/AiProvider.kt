package com.novaai.assistant.ai

/**
 * Kontrak umum untuk setiap provider AI.
 * Provider baru cukup mengimplementasikan interface ini dan didaftarkan di NetworkModule.
 */
interface AiProvider {
    val id: ProviderId

    /** true jika kredensial atau endpoint sudah tersedia. */
    suspend fun isConfigured(): Boolean

    /** Mengirim satu pesan. Tidak boleh melempar exception ke pemanggil. */
    suspend fun send(request: AiRequest): AiResult
}
