package com.swadratna.swadratna_admin.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.swadratna.swadratna_admin.data.model.LoginRequest
import com.swadratna.swadratna_admin.data.model.LoginResponse
import com.swadratna.swadratna_admin.data.remote.api.AuthApiService
import com.swadratna.swadratna_admin.utils.SharedPrefsManager
import com.swadratna.swadratna_admin.utils.SessionManager
import javax.inject.Inject
import com.swadratna.swadratna_admin.utils.NetworkErrorHandler

@RequiresApi(Build.VERSION_CODES.O)
class AuthRepositoryImpl @Inject constructor(
    private val authApiService: AuthApiService,
    private val sharedPrefsManager: SharedPrefsManager,
    private val sessionManager: SessionManager
) : AuthRepository {

    override suspend fun login(loginRequest: LoginRequest): Result<LoginResponse> {
        return try {
            val response = authApiService.login(loginRequest)
            sharedPrefsManager.saveAuthToken(response.token)
            sharedPrefsManager.saveRefreshToken(response.refreshToken)
            sessionManager.startSession()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(Exception(NetworkErrorHandler.getErrorMessage(e), e))
        }
    }

    override fun getAuthToken(): String? {
        return sharedPrefsManager.getAuthToken()
    }

    override fun getRefreshToken(): String? {
        return sharedPrefsManager.getRefreshToken()
    }

    override fun logout() {
        sessionManager.endSession()
    }
    
    override fun isSessionValid(): Boolean {
        return sessionManager.isSessionValid()
    }
}