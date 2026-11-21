package com.novaai.assistant.data.repository

import com.novaai.assistant.ai.AiRequest
import com.novaai.assistant.ai.AiResult
import com.novaai.assistant.ai.AiRouter
import com.novaai.assistant.ai.RoutingConfig

class ChatRepository(private val router: AiRouter) {

    suspend fun ask(prompt: String, sessionId: String, config: RoutingConfig): AiResult {
        return router.send(AiRequest(prompt = prompt, sessionId = sessionId), config)
    }
}
