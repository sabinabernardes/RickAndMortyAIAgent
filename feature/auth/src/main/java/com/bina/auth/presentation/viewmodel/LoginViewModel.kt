package com.bina.auth.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bina.analytics.AnalyticsTracker
import com.bina.auth.analytics.AuthEvent
import com.bina.auth.domain.model.AuthResult
import com.bina.auth.domain.usecase.LoginUseCase
import com.bina.auth.presentation.state.LoginUiState
import com.bina.logging.AppLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    private val logger: AppLogger,
    private val analytics: AnalyticsTracker
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState

    fun onLoginClicked(email: String, password: String) {
        if (_uiState.value is LoginUiState.Loading) return

        analytics.track(AuthEvent.LoginAttempt)
        logger.debug(TAG, "login attempt email=${email.length}chars")

        _uiState.value = LoginUiState.Loading

        viewModelScope.launch {
            val result = loginUseCase(email, password)
            logger.debug(TAG, "login result=${result::class.simpleName}")

            val newState = when (result) {
                is AuthResult.Success -> LoginUiState.Success
                is AuthResult.InvalidEmail -> LoginUiState.Error("Email inválido.")
                is AuthResult.WeakPassword -> LoginUiState.Error("A senha deve ter no mínimo 8 caracteres.")
                is AuthResult.InvalidCredentials -> LoginUiState.Error("Credenciais inválidas.")
            }
            if (newState is LoginUiState.Success) {
                analytics.track(AuthEvent.LoginSuccess)
            } else {
                analytics.track(AuthEvent.LoginFailure)
            }
            _uiState.value = newState
        }
    }

    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }

    companion object {
        private const val TAG = "LoginViewModel"
    }
}
