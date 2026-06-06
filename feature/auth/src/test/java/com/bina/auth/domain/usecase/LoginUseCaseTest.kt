package com.bina.auth.domain.usecase

import com.bina.auth.domain.model.AuthResult
import com.bina.auth.domain.model.UserSession
import com.bina.auth.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LoginUseCaseTest {

    private val repository = mockk<AuthRepository>()
    private lateinit var useCase: LoginUseCase

    @Before
    fun setup() {
        useCase = LoginUseCase(repository)
    }

    @Test
    fun `invoke delegates to repository and passes through AuthResult`() = runTest {
        coEvery { repository.login(any(), any()) } returns AuthResult.InvalidEmail

        val result = useCase("rick@citadel.com", "portal123")

        assertTrue(result is AuthResult.InvalidEmail)
    }

    @Test
    fun `invoke normalizes email before calling repository`() = runTest {
        coEvery { repository.login("rick@citadel.com", any()) } returns AuthResult.Success(
            UserSession(token = "t", email = "rick@citadel.com")
        )

        useCase("  RICK@CITADEL.COM  ", "portal123")

        coVerify { repository.login("rick@citadel.com", "portal123") }
    }
}
