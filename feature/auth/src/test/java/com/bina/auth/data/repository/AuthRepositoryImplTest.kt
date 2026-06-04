package com.bina.auth.data.repository

import com.bina.auth.domain.model.AuthResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AuthRepositoryImplTest {

    private lateinit var storage: FakeSecureStorage
    private lateinit var repository: AuthRepositoryImpl

    @Before
    fun setup() {
        storage = FakeSecureStorage()
        repository = AuthRepositoryImpl(storage)
    }

    @Test
    fun `login with valid credentials returns Success and saves token`() = runTest {
        val result = repository.login("rick@citadel.com", "portal123")

        assertTrue(result is AuthResult.Success)
        assertNotNull(storage.get("auth_token"))
        assertEquals("rick@citadel.com", storage.get("auth_email"))
    }

    @Test
    fun `login with invalid email returns InvalidEmail`() = runTest {
        val result = repository.login("not-an-email", "portal123")

        assertTrue(result is AuthResult.InvalidEmail)
        assertTrue(storage.isEmpty())
    }

    @Test
    fun `login with short password returns WeakPassword`() = runTest {
        val result = repository.login("rick@citadel.com", "short")

        assertTrue(result is AuthResult.WeakPassword)
        assertTrue(storage.isEmpty())
    }

    @Test
    fun `login with exactly 8 char password succeeds`() = runTest {
        val result = repository.login("morty@earth.com", "12345678")

        assertTrue(result is AuthResult.Success)
    }

    @Test
    fun `logout clears storage`() = runTest {
        repository.login("rick@citadel.com", "portal123")
        assertNotNull(storage.get("auth_token"))

        repository.logout()

        assertTrue(storage.isEmpty())
    }

    @Test
    fun `getStoredSession returns null when no session saved`() {
        assertNull(repository.getStoredSession())
    }

    @Test
    fun `getStoredSession returns session after successful login`() = runTest {
        repository.login("rick@citadel.com", "portal123")

        val session = repository.getStoredSession()

        assertNotNull(session)
        assertEquals("rick@citadel.com", session?.email)
        assertNotNull(session?.token)
    }

    @Test
    fun `token has JWT structure`() = runTest {
        val result = repository.login("rick@citadel.com", "portal123") as AuthResult.Success

        val parts = result.session.token.split(".")
        assertEquals(3, parts.size)
    }
}
