package com.bina.auth.domain.usecase

import com.bina.auth.domain.model.AuthResult
import com.bina.auth.domain.repository.AuthRepository

class LoginUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): AuthResult {
        val cleanEmail = email.trim().lowercase()
        return when {
            !cleanEmail.matches(EMAIL_REGEX) -> AuthResult.InvalidEmail
            password.length < MIN_PASSWORD_LENGTH -> AuthResult.WeakPassword
            // demo: simula rejeição server-side para estudar o fluxo de InvalidCredentials
            cleanEmail == DEMO_BLOCKED_EMAIL -> AuthResult.InvalidCredentials
            else -> AuthResult.Success(repository.login(cleanEmail, password))
        }
    }

    companion object {
        private val EMAIL_REGEX = Regex("^[a-zA-Z0-9+_.-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")
        private const val MIN_PASSWORD_LENGTH = 8
        const val DEMO_BLOCKED_EMAIL = "blocked@example.com"
    }
}
