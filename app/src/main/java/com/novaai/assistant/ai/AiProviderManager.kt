package com.novaai.assistant.ai

class AiProviderManager(val all: List<AiProvider>) {

    private val byId: Map<ProviderId, AiProvider> = all.associateBy { it.id }

    fun get(id: ProviderId): AiProvider? = byId[id]

    suspend fun statusLabel(id: ProviderId, config: RoutingConfig): String {
        val provider = get(id) ?: return "Tidak tersedia"
        if (!provider.isConfigured()) return "Belum dikonfigurasi"
        if (id !in config.enabled) return "Nonaktif"
        return "Aktif (koneksi belum diuji)"
    }

    suspend fun statusMap(config: RoutingConfig): Map<ProviderId, String> {
        val result = mutableMapOf<ProviderId, String>()
        for (id in ProviderId.entries) {
            result[id] = statusLabel(id, config)
        }
        return result
    }
}
