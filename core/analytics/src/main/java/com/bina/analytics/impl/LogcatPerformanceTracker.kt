package com.bina.analytics.impl

import android.os.SystemClock
import com.bina.analytics.PerformanceTracker
import com.bina.logging.AppLogger

class LogcatPerformanceTracker(
    private val logger: AppLogger,
    private val clock: () -> Long = { SystemClock.elapsedRealtime() }
) : PerformanceTracker {

    private val activeTraces = mutableMapOf<String, Long>()

    override fun startTrace(name: String) {
        activeTraces[name] = clock()
    }

    override fun stopTrace(name: String): Long {
        val start = activeTraces.remove(name)
        if (start == null) {
            logger.warn(TAG, "[PERF] stopTrace called without startTrace: $name")
            return -1L
        }
        val duration = clock() - start
        logger.info(TAG, "[PERF] $name: ${duration}ms")
        return duration
    }

    companion object {
        private const val TAG = "Performance"
    }
}
