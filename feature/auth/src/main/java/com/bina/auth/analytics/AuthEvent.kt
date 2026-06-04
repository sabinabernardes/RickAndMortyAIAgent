package com.bina.auth.analytics

import com.bina.analytics.event.AnalyticsEvent

sealed class AuthEvent : AnalyticsEvent {

    object LoginAttempt : AuthEvent() {
        override val name = "auth_login_attempt"
    }

    object LoginSuccess : AuthEvent() {
        override val name = "auth_login_success"
    }

    object LoginFailure : AuthEvent() {
        override val name = "auth_login_failure"
    }

    object LogoutRequested : AuthEvent() {
        override val name = "auth_logout"
    }
}
