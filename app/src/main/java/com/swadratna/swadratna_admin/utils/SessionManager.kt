package com.swadratna.swadratna_admin.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    private val sharedPrefsManager: SharedPrefsManager,
    private val jwtUtils: JwtUtils
) {
    
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()
    
    private val _sessionExpired = MutableStateFlow(false)
    val sessionExpired: StateFlow<Boolean> = _sessionExpired.asStateFlow()
    
    init {
        checkSessionValidity()
    }
    
    /**
     * Check if user has a valid session
     */
    fun isSessionValid(): Boolean {
        val token = sharedPrefsManager.getAuthToken()
        
        return when {
            token.isNullOrEmpty() -> false
            jwtUtils.isTokenExpired(token) -> {
                // Token expired based on JWT expiry
                handleSessionExpiration()
                false
            }
            else -> true
        }
    }
    
    /**
     * Start a new session when user logs in
     */
    fun startSession() {
        _isLoggedIn.value = true
        _sessionExpired.value = false
    }
    
    /**
     * End the current session and clear all data
     */
    fun endSession() {
        sharedPrefsManager.clearTokens()
        _isLoggedIn.value = false
        _sessionExpired.value = false
    }
    
    /**
     * Handle session expiration
     */
    private fun handleSessionExpiration() {
        sharedPrefsManager.clearTokens()
        _isLoggedIn.value = false
        _sessionExpired.value = true
    }
    

    /**
     * Check session validity and update state
     */
    private fun checkSessionValidity() {
        _isLoggedIn.value = isSessionValid()
    }
    
    /**
     * Reset session expired flag
     */
    fun resetSessionExpiredFlag() {
        _sessionExpired.value = false
    }
    
    /**
     * Get token expiry time
     */
    fun getTokenExpiryTime(): Long? {
        val token = sharedPrefsManager.getAuthToken()
        return jwtUtils.getTokenExpiryTime(token)
    }

}