package com.swadratna.swadratna_admin.data.remote.api

import com.swadratna.swadratna_admin.data.model.LoginRequest
import com.swadratna.swadratna_admin.data.model.LoginResponse
import com.swadratna.swadratna_admin.data.model.RefreshTokenRequest
import com.swadratna.swadratna_admin.data.model.RefreshTokenResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("api/v1/auth/admin/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("api/v1/auth/refresh-token")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): RefreshTokenResponse
}