package com.bina.auth.data.repository

import com.bina.auth.domain.model.UserSession
import com.bina.auth.domain.repository.AuthRepository
import com.bina.security.storage.SecureStorage
import kotlinx.coroutines.delay
import java.util.Base64

class AuthRepositoryImpl(
    private val secureStorage: SecureStorage
) : AuthRepository {

    override suspend fun login(email: String, password: String): UserSession {
        delay(LOGIN_DELAY_MS)
        val token = buildMockJwt(email)
        secureStorage.save(KEY_TOKEN, token)
        secureStorage.save(KEY_EMAIL, email)
        return UserSession(token = token, email = email)
    }

    override fun logout() {
        secureStorage.clear()
    }

    override fun getStoredSession(): UserSession? {
        val token = secureStorage.get(KEY_TOKEN)
        val email = secureStorage.get(KEY_EMAIL)
        return if (token != null && email != null) UserSession(token = token, email = email) else null
    }

    private fun buildMockJwt(email: String): String {
        val encoder = Base64.getUrlEncoder().withoutPadding()
        val header = encoder.encodeToString("""{"alg":"HS256","typ":"JWT"}""".toByteArray())
        val now = System.currentTimeMillis() / MILLIS_TO_SECONDS
        val payloadJson = """{"sub":"$email","iat":$now,"exp":${now + TOKEN_TTL_SECONDS}}"""
        val payload = encoder.encodeToString(payloadJson.toByteArray())
        // assinatura fake — identificada claramente para fins educacionais
        return "$header.$payload.MOCK_SIGNATURE"
    }

    companion object {
        private const val KEY_TOKEN = "auth_token"
        private const val KEY_EMAIL = "auth_email"
        private const val LOGIN_DELAY_MS = 800L
        private const val TOKEN_TTL_SECONDS = 3600L
        private const val MILLIS_TO_SECONDS = 1000L
    }
}
