package com.swadratna.swadratna_admin.data.repository

import com.swadratna.swadratna_admin.data.model.Analytics
import com.swadratna.swadratna_admin.data.model.AdminAnalyticsDto
import com.swadratna.swadratna_admin.data.model.toDomain
import com.swadratna.swadratna_admin.data.remote.api.AnalyticsApi
import javax.inject.Inject
import javax.inject.Singleton

interface AnalyticsRepository {
    suspend fun loadDashboard(
        franchise: String?,
        from: String?,
        to: String?
    ): Analytics
}

@Singleton
class AnalyticsRepositoryImpl @Inject constructor(
    private val api: AnalyticsApi,
) : AnalyticsRepository {

    override suspend fun loadDashboard(franchise: String?, from: String?, to: String?): Analytics {
        val dto: AdminAnalyticsDto = api.getDashboard(franchise, from, to)
        return dto.toDomain()
    }
}
