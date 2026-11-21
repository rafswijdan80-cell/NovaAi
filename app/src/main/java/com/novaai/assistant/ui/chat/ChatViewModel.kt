package com.novaai.assistant.ui.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.novaai.assistant.ai.AiProviderManager
import com.novaai.assistant.ai.AiResult
import com.novaai.assistant.ai.AiRouter
import com.novaai.assistant.ai.ProviderId
import com.novaai.assistant.ai.Role
import com.novaai.assistant.ai.SelectionMode
import com.novaai.assistant.data.repository.ChatRepository
import com.novaai.assistant.network.NetworkModule
import com.novaai.assistant.storage.ProviderSettingsDataStore
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val settings = ProviderSettingsDataStore(application)
    private val manager: AiProviderManager = NetworkModule.createProviderManager()
    private val repository = ChatRepository(AiRouter(manager))

    private val _state = MutableStateFlow(ChatUiState())
    val state: StateFlow<ChatUiState> = _state.asStateFlow()

    private var conversationSessionId: String = newSessionId()
    private var nextId: Long = 0L

    init {
        viewModelScope.launch {
            settings.config.collect { config ->
                val statuses = manager.statusMap(config)
                _state.value = _state.value.copy(config = config, statuses = statuses)
            }
        }
    }

    fun onInputChange(text: String) {
        _state.value = _state.value.copy(input = text)
    }

    /** Hanya dipanggil saat pengguna menekan tombol Kirim. */
    fun send() {
        val current = _state.value
        val prompt = current.input.trim()
        if (prompt.isEmpty() || current.isLoading) return

        val userMessage = ChatMessage(id = nextId++, role = Role.USER, text = prompt)
        _state.value = current.copy(
            messages = current.messages + userMessage,
            input = "",
            isLoading = true
        )

        viewModelScope.launch {
            val result = repository.ask(prompt, conversationSessionId, current.config)
            val reply = when (result) {
                is AiResult.Success -> ChatMessage(
                    id = nextId++,
                    role = Role.ASSISTANT,
                    text = result.reply.text,
                    providerName = result.reply.providerId.displayName
                )
                is AiResult.Failure -> ChatMessage(
                    id = nextId++,
                    role = Role.ASSISTANT,
                    text = result.error.message,
                    isError = true
                )
            }
            _state.value = _state.value.copy(
                messages = _state.value.messages + reply,
                isLoading = false
            )
        }
    }

    fun newConversation() {
        if (_state.value.isLoading) return
        conversationSessionId = newSessionId()
        _state.value = _state.value.copy(messages = emptyList())
    }

    fun setMode(mode: SelectionMode) {
        viewModelScope.launch { settings.setMode(mode) }
    }

    fun setManualProvider(id: ProviderId) {
        viewModelScope.launch { settings.setManualProvider(id) }
    }

    private fun newSessionId(): String = UUID.randomUUID().toString()
}
