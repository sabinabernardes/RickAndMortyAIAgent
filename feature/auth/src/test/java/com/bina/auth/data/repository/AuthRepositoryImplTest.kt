package com.bina.auth.data.repository

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
    fun `login saves token and email and returns session`() = runTest {
        val session = repository.login("rick@citadel.com", "portal123")

        assertEquals("rick@citadel.com", session.email)
        assertNotNull(session.token)
        assertEquals(session.token, storage.get("auth_token"))
        assertEquals("rick@citadel.com", storage.get("auth_email"))
    }

    @Test
    fun `token has JWT structure`() = runTest {
        val session = repository.login("rick@citadel.com", "portal123")

        val parts = session.token.split(".")
        assertEquals(3, parts.size)
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
}
