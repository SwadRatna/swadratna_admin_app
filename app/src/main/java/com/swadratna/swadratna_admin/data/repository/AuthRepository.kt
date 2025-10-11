package com.swadratna.swadratna_admin.data.repository

import com.swadratna.swadratna_admin.data.model.LoginRequest
import com.swadratna.swadratna_admin.data.model.LoginResponse

interface AuthRepository {
    suspend fun login(loginRequest: LoginRequest): Result<LoginResponse>
    fun getAuthToken(): String?
    fun getRefreshToken(): String?
    fun logout()
    fun isSessionValid(): Boolean
}