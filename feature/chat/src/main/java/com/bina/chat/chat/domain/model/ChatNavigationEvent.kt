package com.bina.chat.chat.domain.model

sealed class ChatNavigationEvent {
    data class OpenCharacter(val characterId: Int) : ChatNavigationEvent()
    data class SearchCharacters(val query: String) : ChatNavigationEvent()
}
