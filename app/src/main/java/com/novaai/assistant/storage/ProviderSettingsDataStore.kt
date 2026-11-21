package com.novaai.assistant.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.novaai.assistant.ai.ProviderId
import com.novaai.assistant.ai.RoutingConfig
import com.novaai.assistant.ai.SelectionMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.providerSettingsStore: DataStore<Preferences> by preferencesDataStore(
    name = "nova_provider_settings"
)

/** Menyimpan pengaturan provider lokal. API key TIDAK disimpan di sini. */
class ProviderSettingsDataStore(private val context: Context) {

    private val modeKey = stringPreferencesKey("routing_mode")
    private val manualKey = stringPreferencesKey("manual_provider")
    private val primaryKey = stringPreferencesKey("primary_provider")
    private val backupKey = stringPreferencesKey("backup_provider")
    private val enabledKey = stringSetPreferencesKey("enabled_providers")
    private val geminiConsentKey = booleanPreferencesKey("gemini_consent")

    val config: Flow<RoutingConfig> = context.providerSettingsStore.data.map { prefs ->
        RoutingConfig(
            mode = parseMode(prefs[modeKey]) ?: SelectionMode.AUTO,
            manualProvider = parseProvider(prefs[manualKey]) ?: ProviderId.OFFICIAL_PRIMARY,
            primary = parseProvider(prefs[primaryKey]) ?: ProviderId.OFFICIAL_PRIMARY,
            backup = parseProvider(prefs[backupKey]),
            enabled = prefs[enabledKey]
                ?.mapNotNull { parseProvider(it) }
                ?.toSet()
                ?: emptySet(),
            geminiConsent = prefs[geminiConsentKey] ?: false
        )
    }

    suspend fun setMode(mode: SelectionMode) {
        context.providerSettingsStore.edit { it[modeKey] = mode.name }
    }

    suspend fun setManualProvider(id: ProviderId) {
        context.providerSettingsStore.edit { it[manualKey] = id.name }
    }

    suspend fun setPrimary(id: ProviderId) {
        context.providerSettingsStore.edit { it[primaryKey] = id.name }
    }

    suspend fun setBackup(id: ProviderId?) {
        context.providerSettingsStore.edit { prefs ->
            if (id == null) prefs.remove(backupKey) else prefs[backupKey] = id.name
        }
    }

    suspend fun setEnabled(id: ProviderId, enabled: Boolean) {
        context.providerSettingsStore.edit { prefs ->
            val current = prefs[enabledKey]?.toMutableSet() ?: mutableSetOf()
            if (enabled) current.add(id.name) else current.remove(id.name)
            prefs[enabledKey] = current
        }
    }

    /**
     * Mengaktifkan Gemini Tambahan sekaligus mencatat persetujuan.
     * Hanya dipanggil dari ProviderViewModel setelah pengguna menekan "Saya setuju".
     */
    suspend fun approveGeminiAndEnable() {
        context.providerSettingsStore.edit { prefs ->
            prefs[geminiConsentKey] = true
            val current = prefs[enabledKey]?.toMutableSet() ?: mutableSetOf()
            current.add(ProviderId.GEMINI_TAMBAHAN.name)
            prefs[enabledKey] = current
        }
    }

    /** Mematikan Gemini Tambahan dan mencabut persetujuan. Persetujuan harus diberikan ulang. */
    suspend fun revokeGemini() {
        context.providerSettingsStore.edit { prefs ->
            prefs[geminiConsentKey] = false
            val current = prefs[enabledKey]?.toMutableSet() ?: mutableSetOf()
            current.remove(ProviderId.GEMINI_TAMBAHAN.name)
            prefs[enabledKey] = current
        }
    }

    private fun parseProvider(name: String?): ProviderId? {
        return ProviderId.entries.firstOrNull { it.name == name }
    }

    private fun parseMode(name: String?): SelectionMode? {
        return SelectionMode.entries.firstOrNull { it.name == name }
    }
}
