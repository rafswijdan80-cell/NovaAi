package com.novaai.assistant.navigation

data class NovaFeature(
    val route: String,
    val title: String,
    val subtitle: String,
    val stage: String
)

object Routes {
    const val DASHBOARD = "dashboard"
}

val NovaFeatures = listOf(
    NovaFeature("chat", "Chat AI", "Percakapan dengan AI", "TAHAP 2"),
    NovaFeature("floating", "Floating Assistant", "Bubble NOVA di atas aplikasi lain", "TAHAP 3"),
    NovaFeature("scan", "Scan Soal", "Kamera, galeri, dan OCR", "TAHAP 4"),
    NovaFeature("ml_analyst", "NOVA ML ANALYST", "Analisis draft Mobile Legends", "TAHAP 5"),
    NovaFeature("history", "Riwayat", "Riwayat chat dan scan lokal", "TAHAP 6"),
    NovaFeature("providers", "Pilihan AI", "Pengaturan provider AI", "TAHAP 2"),
    NovaFeature("upgrade", "Upgrade", "Paket FREE, PRO, dan VIP", "TAHAP 6"),
    NovaFeature("settings", "Pengaturan", "Pengaturan aplikasi", "TAHAP 2"),
    NovaFeature("privacy", "Keamanan & Privasi", "Izin dan data pengguna", "TAHAP 6"),
    NovaFeature("owner", "Dashboard OWNER", "Administrasi berbasis backend", "TAHAP 6")
)
