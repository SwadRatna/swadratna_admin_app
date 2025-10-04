package com.swadratna.swadratna_admin.data.repository

import com.swadratna.swadratna_admin.data.model.Activity
import com.swadratna.swadratna_admin.data.model.ActivityType
import kotlinx.coroutines.flow.Flow

interface ActivityRepository {
    suspend fun addActivity(activity: Activity)
    suspend fun addActivity(
        type: ActivityType,
        title: String,
        description: String,
        entityId: String? = null,
        entityName: String? = null
    )
    fun getAllActivities(): Flow<List<Activity>>
    fun getRecentActivities(limit: Int = 3): Flow<List<Activity>>
    suspend fun clearAllActivities()
}