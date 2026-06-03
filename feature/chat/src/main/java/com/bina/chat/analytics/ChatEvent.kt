package com.bina.chat.analytics

import com.bina.analytics.event.AnalyticsEvent

sealed class ChatEvent : AnalyticsEvent {

    object MessageSent : ChatEvent() {
        override val name = "chat_message_sent"
    }

    object ModelUnavailable : ChatEvent() {
        override val name = "chat_model_unavailable"
    }

    data class AgentNavigationTriggered(val action: String) : ChatEvent() {
        override val name = "chat_agent_navigation"
        override val properties = mapOf("action" to action)
    }
}
