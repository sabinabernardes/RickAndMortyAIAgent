package com.bina.auth.presentation.viewmodel

import com.bina.analytics.AnalyticsTracker
import com.bina.analytics.event.AnalyticsEvent
import com.bina.auth.domain.model.AuthResult
import com.bina.auth.domain.model.UserSession
import com.bina.auth.domain.repository.AuthRepository
import com.bina.auth.domain.usecase.LoginUseCase
import com.bina.auth.presentation.state.LoginUiState
import com.bina.logging.AppLogger
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val repository = mockk<AuthRepository>()
    private val logger = mockk<AppLogger>(relaxed = true)
    private val analytics = mockk<AnalyticsTracker>(relaxed = true)
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = LoginViewModel(LoginUseCase(repository), logger, analytics)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Idle`() {
        assertTrue(viewModel.uiState.value is LoginUiState.Idle)
    }

    @Test
    fun `valid credentials transition to Success`() = runTest {
        coEvery { repository.login(any(), any()) } returns AuthResult.Success(
            UserSession(token = "mock.token.sig", email = "rick@citadel.com")
        )

        viewModel.onLoginClicked("rick@citadel.com", "portal123")

        assertTrue(viewModel.uiState.value is LoginUiState.Success)
    }

    @Test
    fun `invalid email transitions to Error`() = runTest {
        coEvery { repository.login(any(), any()) } returns AuthResult.InvalidEmail

        viewModel.onLoginClicked("bad-email", "portal123")

        val state = viewModel.uiState.value
        assertTrue(state is LoginUiState.Error)
        assertEquals("Email inválido.", (state as LoginUiState.Error).message)
    }

    @Test
    fun `weak password transitions to Error`() = runTest {
        coEvery { repository.login(any(), any()) } returns AuthResult.WeakPassword

        viewModel.onLoginClicked("rick@citadel.com", "short")

        val state = viewModel.uiState.value
        assertTrue(state is LoginUiState.Error)
        assertEquals("A senha deve ter no mínimo 8 caracteres.", (state as LoginUiState.Error).message)
    }

    @Test
    fun `invalid credentials transitions to generic Error`() = runTest {
        coEvery { repository.login(any(), any()) } returns AuthResult.InvalidCredentials

        viewModel.onLoginClicked("rick@citadel.com", "wrongpass")

        val state = viewModel.uiState.value
        assertTrue(state is LoginUiState.Error)
        assertEquals("Credenciais inválidas.", (state as LoginUiState.Error).message)
    }

    @Test
    fun `analytics tracks login attempt on every click`() = runTest {
        coEvery { repository.login(any(), any()) } returns AuthResult.InvalidEmail

        viewModel.onLoginClicked("any@email.com", "anypassword")

        verify { analytics.track(any<AnalyticsEvent>()) }
    }

    @Test
    fun `resetState returns to Idle`() = runTest {
        coEvery { repository.login(any(), any()) } returns AuthResult.Success(
            UserSession(token = "t", email = "e@e.com")
        )
        viewModel.onLoginClicked("e@e.com", "password1")
        assertTrue(viewModel.uiState.value is LoginUiState.Success)

        viewModel.resetState()

        assertTrue(viewModel.uiState.value is LoginUiState.Idle)
    }
}
