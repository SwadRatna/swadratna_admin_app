package com.swadratna.swadratna_admin.data.remote.api

import com.swadratna.swadratna_admin.data.model.DashboardResponse
import retrofit2.http.GET

interface DashboardApi {
    @GET("dashboard")
    suspend fun getDashboardData(): DashboardResponse
}
