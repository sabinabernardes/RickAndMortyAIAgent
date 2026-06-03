package com.bina.character_details.analytics

import com.bina.analytics.event.AnalyticsEvent

sealed class CharacterDetailsEvent : AnalyticsEvent {

    data class ScreenOpened(val characterId: String) : CharacterDetailsEvent() {
        override val name = "character_details_screen_opened"
        override val properties = mapOf("character_id" to characterId)
    }

    data class EpisodesLoaded(val episodeCount: Int) : CharacterDetailsEvent() {
        override val name = "character_details_episodes_loaded"
        override val properties = mapOf("episode_count" to episodeCount.toString())
    }

    object EpisodesLoadFailed : CharacterDetailsEvent() {
        override val name = "character_details_episodes_load_failed"
    }
}
