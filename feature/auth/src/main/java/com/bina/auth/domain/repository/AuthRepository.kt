package com.bina.auth.domain.repository

import com.bina.auth.domain.model.UserSession

interface AuthRepository {
    suspend fun login(email: String, password: String): UserSession
    fun logout()
    fun getStoredSession(): UserSession?
}
