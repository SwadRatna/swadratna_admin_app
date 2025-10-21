package com.swadratna.swadratna_admin.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swadratna.swadratna_admin.data.repository.AuthRepository
import com.swadratna.swadratna_admin.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        checkAuthenticationStatus()
        observeSessionState()
    }

    /**
     * Check initial authentication status
     */
    private fun checkAuthenticationStatus() {
        viewModelScope.launch {
            val isAuthenticated = authRepository.isSessionValid()
            _authState.value = _authState.value.copy(
                isAuthenticated = isAuthenticated,
                isLoading = false
            )
        }
    }

    /**
     * Observe session state changes
     */
    private fun observeSessionState() {
        viewModelScope.launch {
            combine(
                sessionManager.isLoggedIn,
                sessionManager.sessionExpired
            ) { isLoggedIn, sessionExpired ->
                when {
                    sessionExpired -> {
                        _authState.value = _authState.value.copy(
                            isAuthenticated = false,
                            sessionExpired = true,
                            isLoading = false
                        )
                    }
                    isLoggedIn -> {
                        _authState.value = _authState.value.copy(
                            isAuthenticated = true,
                            sessionExpired = false,
                            isLoading = false
                        )
                    }
                    else -> {
                        _authState.value = _authState.value.copy(
                            isAuthenticated = false,
                            sessionExpired = false,
                            isLoading = false
                        )
                    }
                }
            }.collect { }
        }
    }

    /**
     * Handle logout
     */
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _authState.value = AuthState(
                isAuthenticated = false,
                sessionExpired = false,
                isLoading = false
            )
        }
    }

    /**
     * Reset session expired flag
     */
    fun resetSessionExpired() {
        sessionManager.resetSessionExpiredFlag()
        _authState.value = _authState.value.copy(sessionExpired = false)
    }

    /**
     * Update session time (call this on user activity)
     */

}

data class AuthState(
    val isAuthenticated: Boolean = false,
    val sessionExpired: Boolean = false,
    val isLoading: Boolean = true
)