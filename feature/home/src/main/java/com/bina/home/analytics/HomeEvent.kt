package com.bina.home.analytics

import com.bina.analytics.event.AnalyticsEvent

sealed class HomeEvent : AnalyticsEvent {

    data class CharacterClicked(val characterId: String) : HomeEvent() {
        override val name = "home_character_clicked"
        override val properties = mapOf("character_id" to characterId)
    }

    data class SearchPerformed(val query: String) : HomeEvent() {
        override val name = "home_search_performed"
        override val properties = mapOf("query" to query)
    }

    object PaginationLoadedNextPage : HomeEvent() {
        override val name = "home_pagination_next_page"
    }
}
