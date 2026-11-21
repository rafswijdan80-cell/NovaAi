package com.novaai.assistant.ai

enum class ProviderId(val displayName: String) {
    OFFICIAL_PRIMARY("AI Utama (Resmi)"),
    GEMINI_TAMBAHAN("Gemini Tambahan")
}

enum class SelectionMode { AUTO, MANUAL }

enum class Role { USER, ASSISTANT }

data class RoutingConfig(
    val mode: SelectionMode = SelectionMode.AUTO,
    val manualProvider: ProviderId = ProviderId.OFFICIAL_PRIMARY,
    val primary: ProviderId = ProviderId.OFFICIAL_PRIMARY,
    val backup: ProviderId? = null,
    val enabled: Set<ProviderId> = emptySet(),
    /** Persetujuan peringatan privasi untuk Gemini Tambahan (metode GET). */
    val geminiConsent: Boolean = false
)

data class AiRequest(
    val prompt: String,
    val sessionId: String
)

data class AiReply(
    val text: String,
    val providerId: ProviderId,
    val remoteSessionId: String?
)

sealed class AiError(val message: String) {
    class NotConfigured(id: ProviderId) :
        AiError("${id.displayName} belum dikonfigurasi.")

    class Disabled(id: ProviderId) :
        AiError("${id.displayName} sedang nonaktif. Aktifkan di menu Pilihan AI.")

    class ConsentRequired(id: ProviderId) :
        AiError("${id.displayName} belum disetujui. Setujui peringatan privasi di menu Pilihan AI.")

    object Timeout : AiError("Permintaan melewati batas waktu. Coba lagi.")

    object NetworkUnavailable :
        AiError("Koneksi gagal. Periksa koneksi internet kamu lalu coba lagi.")

    object InvalidResponse : AiError("Respons dari provider tidak valid.")

    class Provider(detail: String) : AiError(detail)

    object NoActiveProvider :
        AiError("Tidak ada provider aktif yang terkonfigurasi. Atur di menu Pilihan AI.")

    object UnknownProvider : AiError("Provider tidak ditemukan.")
}

sealed class AiResult {
    data class Success(val reply: AiReply) : AiResult()
    data class Failure(val error: AiError) : AiResult()
}
