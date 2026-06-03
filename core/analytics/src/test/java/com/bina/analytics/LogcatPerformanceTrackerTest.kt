package com.bina.analytics

import com.bina.analytics.impl.LogcatPerformanceTracker
import com.bina.logging.AppLogger
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LogcatPerformanceTrackerTest {

    private val logger: AppLogger = mockk(relaxed = true)
    private var fakeClock = 0L
    private lateinit var tracker: PerformanceTracker

    @Before
    fun setUp() {
        fakeClock = 0L
        tracker = LogcatPerformanceTracker(logger, clock = { fakeClock })
    }

    @Test
    fun `GIVEN startTrace and stopTrace WHEN called THEN returns correct duration`() {
        fakeClock = 100L
        tracker.startTrace("load_characters")
        fakeClock = 250L
        val duration = tracker.stopTrace("load_characters")
        assertEquals(150L, duration)
    }

    @Test
    fun `GIVEN startTrace and stopTrace WHEN called THEN logs perf result`() {
        fakeClock = 0L
        tracker.startTrace("load_characters")
        fakeClock = 100L
        tracker.stopTrace("load_characters")
        verify { logger.info("Performance", match { it.contains("[PERF] load_characters:") && it.contains("ms") }) }
    }

    @Test
    fun `GIVEN stopTrace without startTrace WHEN called THEN returns -1 and logs warning`() {
        val duration = tracker.stopTrace("unknown_trace")
        assertEquals(-1L, duration)
        verify { logger.warn("Performance", match { it.contains("stopTrace called without startTrace") }) }
    }

    @Test
    fun `GIVEN two independent traces WHEN both stopped THEN each returns its own duration`() {
        fakeClock = 0L
        tracker.startTrace("trace_a")
        fakeClock = 50L
        tracker.startTrace("trace_b")
        fakeClock = 100L
        val durationA = tracker.stopTrace("trace_a")
        fakeClock = 200L
        val durationB = tracker.stopTrace("trace_b")
        assertEquals(100L, durationA)
        assertEquals(150L, durationB)
    }

    @Test
    fun `GIVEN zero elapsed time WHEN stopTrace THEN returns 0`() {
        fakeClock = 500L
        tracker.startTrace("instant_op")
        val duration = tracker.stopTrace("instant_op")
        assertEquals(0L, duration)
        assertTrue(duration >= 0L)
    }
}
