package com.novaai.assistant.ui.chat

import com.novaai.assistant.ai.ProviderId
import com.novaai.assistant.ai.Role
import com.novaai.assistant.ai.RoutingConfig

data class ChatMessage(
    val id: Long,
    val role: Role,
    val text: String,
    val providerName: String? = null,
    val isError: Boolean = false
)

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val input: String = "",
    val isLoading: Boolean = false,
    val config: RoutingConfig = RoutingConfig(),
    val statuses: Map<ProviderId, String> = emptyMap()
)
