package com.bina.analytics

import com.bina.analytics.event.AnalyticsEvent

interface AnalyticsTracker {
    fun track(event: AnalyticsEvent)
}
