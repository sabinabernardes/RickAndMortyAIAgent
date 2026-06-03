package com.bina.analytics

interface PerformanceTracker {
    fun startTrace(name: String)
    fun stopTrace(name: String): Long
}
