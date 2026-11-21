package com.novaai.assistant.ui.providers

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.novaai.assistant.ai.AiProviderManager
import com.novaai.assistant.ai.ProviderId
import com.novaai.assistant.ai.RoutingConfig
import com.novaai.assistant.ai.SelectionMode
import com.novaai.assistant.network.NetworkModule
import com.novaai.assistant.storage.ProviderSettingsDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProviderUiState(
    val config: RoutingConfig = RoutingConfig(),
    val statuses: Map<ProviderId, String> = emptyMap(),
    val showGeminiConsent: Boolean = false
)

class ProviderViewModel(application: Application) : AndroidViewModel(application) {

    private val settings = ProviderSettingsDataStore(application)
    private val manager: AiProviderManager = NetworkModule.createProviderManager()

    private val _state = MutableStateFlow(ProviderUiState())
    val state: StateFlow<ProviderUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            settings.config.collect { config ->
                val statuses = manager.statusMap(config)
                _state.value = _state.value.copy(config = config, statuses = statuses)
            }
        }
    }

    fun setEnabled(id: ProviderId, enabled: Boolean) {
        if (id == ProviderId.GEMINI_TAMBAHAN) {
            if (!enabled) {
                viewModelScope.launch { settings.revokeGemini() }
            } else if (_state.value.config.geminiConsent) {
                viewModelScope.launch { settings.approveGeminiAndEnable() }
            } else {
                // Belum pernah disetujui: tampilkan peringatan dulu.
                _state.value = _state.value.copy(showGeminiConsent = true)
            }
            return
        }
        viewModelScope.launch { settings.setEnabled(id, enabled) }
    }

    fun confirmGeminiConsent() {
        _state.value = _state.value.copy(showGeminiConsent = false)
        viewModelScope.launch { settings.approveGeminiAndEnable() }
    }

    fun dismissGeminiConsent() {
        _state.value = _state.value.copy(showGeminiConsent = false)
    }

    fun setPrimary(id: ProviderId) {
        viewModelScope.launch {
            settings.setPrimary(id)
            if (_state.value.config.backup == id) {
                settings.setBackup(null)
            }
        }
    }

    fun setBackup(id: ProviderId?) {
        viewModelScope.launch {
            if (id != null && id == _state.value.config.primary) return@launch
            settings.setBackup(id)
        }
    }

    fun setMode(mode: SelectionMode) {
        viewModelScope.launch { settings.setMode(mode) }
    }
}
