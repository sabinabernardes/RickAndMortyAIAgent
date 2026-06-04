package com.bina.auth.domain.model

data class UserSession(
    val token: String,
    val email: String
)
