package com.bina.chat.domain.model

data class AgentMessageResult(
    val text: String,
    val navigationEvent: ChatNavigationEvent? = null
)
