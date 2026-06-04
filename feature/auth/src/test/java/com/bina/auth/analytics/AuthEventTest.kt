package com.bina.auth.analytics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthEventTest {

    @Test
    fun `LoginAttempt has correct name`() {
        assertEquals("auth_login_attempt", AuthEvent.LoginAttempt.name)
    }

    @Test
    fun `LoginSuccess has correct name`() {
        assertEquals("auth_login_success", AuthEvent.LoginSuccess.name)
    }

    @Test
    fun `LoginFailure has correct name`() {
        assertEquals("auth_login_failure", AuthEvent.LoginFailure.name)
    }

    @Test
    fun `LogoutRequested has correct name`() {
        assertEquals("auth_logout", AuthEvent.LogoutRequested.name)
    }

    @Test
    fun `all events have empty properties by default`() {
        val events = listOf(
            AuthEvent.LoginAttempt,
            AuthEvent.LoginSuccess,
            AuthEvent.LoginFailure,
            AuthEvent.LogoutRequested
        )
        events.forEach { assertTrue(it.properties.isEmpty()) }
    }
}
