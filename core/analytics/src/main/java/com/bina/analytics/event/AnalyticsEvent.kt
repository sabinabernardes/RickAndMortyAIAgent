package com.bina.analytics.event

interface AnalyticsEvent {
    val name: String
    val properties: Map<String, String> get() = emptyMap()
}
