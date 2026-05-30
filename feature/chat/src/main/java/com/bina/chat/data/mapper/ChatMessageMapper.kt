package com.bina.chat.data.mapper

import com.bina.chat.data.model.ChatMessageData
import com.bina.chat.domain.model.ChatMessageDomain
import com.bina.chat.domain.model.MessageRole

object ChatMessageMapper {
    fun toDomain(data: ChatMessageData): ChatMessageDomain = ChatMessageDomain(
        role = if (data.role == "user") MessageRole.USER else MessageRole.AI,
        text = data.text,
        timestampMs = data.timestampMs
    )
}
