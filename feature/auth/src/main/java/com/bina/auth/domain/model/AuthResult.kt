package com.bina.auth.domain.model

sealed class AuthResult {
    data class Success(val session: UserSession) : AuthResult()
    object InvalidEmail : AuthResult()

    // senha com menos de 8 caracteres
    object WeakPassword : AuthResult()

    // credenciais semanticamente inválidas — mensagem genérica para não revelar qual campo falhou
    object InvalidCredentials : AuthResult()
}
