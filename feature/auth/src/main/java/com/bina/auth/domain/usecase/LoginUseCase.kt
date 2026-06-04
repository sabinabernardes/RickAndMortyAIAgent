package com.bina.auth.domain.usecase

import com.bina.auth.domain.model.AuthResult
import com.bina.auth.domain.repository.AuthRepository

class LoginUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): AuthResult {
        val cleanEmail = email.trim().lowercase()
        return repository.login(cleanEmail, password)
    }
}
