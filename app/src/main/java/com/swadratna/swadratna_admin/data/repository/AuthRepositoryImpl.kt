package com.swadratna.swadratna_admin.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.swadratna.swadratna_admin.data.model.LoginRequest
import com.swadratna.swadratna_admin.data.model.LoginResponse
import com.swadratna.swadratna_admin.data.remote.api.AuthApiService
import com.swadratna.swadratna_admin.utils.SharedPrefsManager
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
class AuthRepositoryImpl @Inject constructor(
    private val authApiService: AuthApiService,
    private val sharedPrefsManager: SharedPrefsManager
) : AuthRepository {

    override suspend fun login(loginRequest: LoginRequest): Result<LoginResponse> {
        return try {
            val response = authApiService.login(loginRequest)
            sharedPrefsManager.saveAuthToken(response.token)
            sharedPrefsManager.saveRefreshToken(response.refreshToken)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getAuthToken(): String? {
        return sharedPrefsManager.getAuthToken()
    }

    override fun getRefreshToken(): String? {
        return sharedPrefsManager.getRefreshToken()
    }

    override fun logout() {
        sharedPrefsManager.clearTokens()
    }
}