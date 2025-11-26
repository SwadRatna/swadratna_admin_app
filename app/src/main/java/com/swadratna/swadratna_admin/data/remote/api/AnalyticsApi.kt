package com.swadratna.swadratna_admin.data.remote.api

import com.swadratna.swadratna_admin.data.model.AdminAnalyticsDto
import retrofit2.http.GET
import retrofit2.http.Query

interface AnalyticsApi {
    @GET("api/v1/admin/dashboard/analytics")
    suspend fun getDashboard(
        @Query("franchise") franchise: String?,
        @Query("from") from: String?,
        @Query("to") to: String?
    ): AdminAnalyticsDto
}
