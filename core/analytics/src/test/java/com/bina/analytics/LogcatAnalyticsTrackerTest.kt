package com.bina.analytics

import com.bina.analytics.event.AnalyticsEvent
import com.bina.analytics.impl.LogcatAnalyticsTracker
import com.bina.logging.AppLogger
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class LogcatAnalyticsTrackerTest {

    private val logger: AppLogger = mockk(relaxed = true)
    private lateinit var tracker: AnalyticsTracker

    @Before
    fun setUp() {
        tracker = LogcatAnalyticsTracker(logger)
    }

    @Test
    fun `GIVEN event with no properties WHEN track THEN logs only event name`() {
        val event = object : AnalyticsEvent {
            override val name = "test_event"
        }
        tracker.track(event)
        verify { logger.info("Analytics", "[EVENT] test_event") }
    }

    @Test
    fun `GIVEN event with properties WHEN track THEN logs name and properties`() {
        val event = object : AnalyticsEvent {
            override val name = "character_clicked"
            override val properties = mapOf("character_id" to "42")
        }
        tracker.track(event)
        verify { logger.info("Analytics", "[EVENT] character_clicked | character_id=42") }
    }

    @Test
    fun `GIVEN event with multiple properties WHEN track THEN logs all properties`() {
        val event = object : AnalyticsEvent {
            override val name = "search_performed"
            override val properties = mapOf("query" to "Rick", "page" to "1")
        }
        tracker.track(event)
        verify { logger.info("Analytics", match { it.contains("search_performed") && it.contains("query=Rick") && it.contains("page=1") }) }
    }
}
