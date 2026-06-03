package com.bina.analytics.impl

import com.bina.analytics.AnalyticsTracker
import com.bina.analytics.event.AnalyticsEvent
import com.bina.logging.AppLogger

class LogcatAnalyticsTracker(private val logger: AppLogger) : AnalyticsTracker {

    override fun track(event: AnalyticsEvent) {
        val props = if (event.properties.isEmpty()) ""
        else " | ${event.properties.entries.joinToString { "${it.key}=${it.value}" }}"
        logger.info(TAG, "[EVENT] ${event.name}$props")
    }

    companion object {
        private const val TAG = "Analytics"
    }
}
