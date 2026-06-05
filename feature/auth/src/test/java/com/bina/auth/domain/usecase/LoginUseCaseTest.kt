package com.bina.auth.domain.usecase

import com.bina.auth.domain.model.AuthResult
import com.bina.auth.domain.model.UserSession
import com.bina.auth.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
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
    fun `invalid email returns InvalidEmail without calling repository`() = runTest {
        val result = useCase("not-an-email", "portal123")

        assertTrue(result is AuthResult.InvalidEmail)
        coVerify(exactly = 0) { repository.login(any(), any()) }
    }

    @Test
    fun `short password returns WeakPassword without calling repository`() = runTest {
        val result = useCase("rick@citadel.com", "short")

        assertTrue(result is AuthResult.WeakPassword)
        coVerify(exactly = 0) { repository.login(any(), any()) }
    }

    @Test
    fun `exactly 8 char password is accepted`() = runTest {
        val session = UserSession(token = "t", email = "rick@citadel.com")
        coEvery { repository.login(any(), any()) } returns session

        val result = useCase("rick@citadel.com", "12345678")

        assertTrue(result is AuthResult.Success)
    }

    @Test
    fun `demo blocked email returns InvalidCredentials without calling repository`() = runTest {
        val result = useCase(LoginUseCase.DEMO_BLOCKED_EMAIL, "portal123")

        assertTrue(result is AuthResult.InvalidCredentials)
        coVerify(exactly = 0) { repository.login(any(), any()) }
    }

    @Test
    fun `valid credentials delegate to repository and return Success`() = runTest {
        val session = UserSession(token = "mock.token.sig", email = "rick@citadel.com")
        coEvery { repository.login(any(), any()) } returns session

        val result = useCase("rick@citadel.com", "portal123")

        assertTrue(result is AuthResult.Success)
        assertEquals(session, (result as AuthResult.Success).session)
    }

    @Test
    fun `email is trimmed and lowercased before validation`() = runTest {
        val session = UserSession(token = "t", email = "rick@citadel.com")
        coEvery { repository.login("rick@citadel.com", any()) } returns session

        val result = useCase("  RICK@CITADEL.COM  ", "portal123")

        assertTrue(result is AuthResult.Success)
        coVerify { repository.login("rick@citadel.com", "portal123") }
    }
}
