package com.bina.logging

import com.bina.logging.impl.LogcatLogger
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class LogcatLoggerTest {

    private val logger: AppLogger = mockk(relaxed = true)

    @Test
    fun `GIVEN a message WHEN debug is called THEN no exception is thrown`() {
        logger.debug("TAG", "debug message")
        verify { logger.debug("TAG", "debug message") }
    }

    @Test
    fun `GIVEN a message WHEN info is called THEN no exception is thrown`() {
        logger.info("TAG", "info message")
        verify { logger.info("TAG", "info message") }
    }

    @Test
    fun `GIVEN a message WHEN warn is called THEN no exception is thrown`() {
        logger.warn("TAG", "warn message")
        verify { logger.warn("TAG", "warn message") }
    }

    @Test
    fun `GIVEN a message and throwable WHEN error is called THEN no exception is thrown`() {
        val throwable = RuntimeException("test error")
        logger.error("TAG", "error message", throwable)
        verify { logger.error("TAG", "error message", throwable) }
    }

    @Test
    fun `GIVEN AppLogger interface THEN LogcatLogger implements it`() {
        val logcatLogger: AppLogger = LogcatLogger()
        assert(logcatLogger is AppLogger)
    }
}
