package com.bina.chat.chat.presentation.mapper

import com.bina.chat.chat.domain.model.ChatMessageDomain
import com.bina.chat.chat.presentation.model.ChatMessageUiModel

class ChatMessageUiMapper {
    fun map(domain: ChatMessageDomain): ChatMessageUiModel = ChatMessageUiModel(
        role = domain.role,
        text = domain.text
    )
}
