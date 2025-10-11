package com.swadratna.swadratna_admin.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.reflect.TypeToken
import com.swadratna.swadratna_admin.data.model.Activity
import com.swadratna.swadratna_admin.data.model.ActivityType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.lang.reflect.Type
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

class LocalDateTimeAdapter : JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    
    override fun serialize(src: LocalDateTime?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return JsonPrimitive(src?.format(formatter))
    }
    
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): LocalDateTime {
        return LocalDateTime.parse(json?.asString, formatter)
    }
}

@Singleton
class ActivityRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ActivityRepository {
    
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        "swadratna_activities", Context.MODE_PRIVATE
    )
    private val gson = GsonBuilder()
        .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeAdapter())
        .create()
    private val _activities = MutableStateFlow<List<Activity>>(emptyList())
    
    companion object {
        private const val ACTIVITIES_KEY = "activities_list"
        private const val LAST_CLEANUP_KEY = "last_cleanup_timestamp"
        private const val CLEANUP_INTERVAL_DAYS = 7 // Check for cleanup every week
        private const val ACTIVITY_RETENTION_MONTHS = 2L // Keep activities for 2 months
    }
    
    init {
        loadActivitiesFromStorage()
        performCleanupIfNeeded()
    }

    
    private fun loadActivitiesFromStorage() {
        val activitiesJson = sharedPreferences.getString(ACTIVITIES_KEY, null)
        if (activitiesJson != null) {
            try {
                val type = object : TypeToken<List<Activity>>() {}.type
                val activities: List<Activity> = gson.fromJson(activitiesJson, type)
                _activities.value = activities
            } catch (e: Exception) {
                _activities.value = emptyList()
            }
        } else {
            println("DEBUG ActivityRepository: No activities found in storage, starting with empty list")
        }
    }
    
    private fun saveActivitiesToStorage() {
        val activitiesJson = gson.toJson(_activities.value)
        sharedPreferences.edit()
            .putString(ACTIVITIES_KEY, activitiesJson)
            .apply()
    }
    
    private fun performCleanupIfNeeded() {
        val lastCleanup = sharedPreferences.getLong(LAST_CLEANUP_KEY, 0)
        val now = System.currentTimeMillis()
        val daysSinceLastCleanup = (now - lastCleanup) / (1000 * 60 * 60 * 24)
        
        if (daysSinceLastCleanup >= CLEANUP_INTERVAL_DAYS) {
            cleanupOldActivities()
            sharedPreferences.edit()
                .putLong(LAST_CLEANUP_KEY, now)
                .apply()
        }
    }
    
    private fun cleanupOldActivities() {
        val cutoffDate = LocalDateTime.now().minusMonths(ACTIVITY_RETENTION_MONTHS)
        val filteredActivities = _activities.value.filter { activity ->
            activity.timestamp.isAfter(cutoffDate)
        }
        
        if (filteredActivities.size != _activities.value.size) {
            _activities.value = filteredActivities
            saveActivitiesToStorage()
        }
    }
    
    override suspend fun addActivity(activity: Activity) {
        val currentActivities = _activities.value.toMutableList()
        currentActivities.add(0, activity)
        
        if (currentActivities.size > 100) {
            currentActivities.removeAt(currentActivities.size - 1)
        }
        
        _activities.value = currentActivities
        saveActivitiesToStorage()
    }
    
    override suspend fun addActivity(
        type: ActivityType,
        title: String,
        description: String,
        entityId: String?,
        entityName: String?
    ) {
        val activity = Activity(
            id = UUID.randomUUID().toString(),
            type = type,
            title = title,
            description = description,
            timestamp = LocalDateTime.now(),
            entityId = entityId,
            entityName = entityName
        )
        addActivity(activity)
    }
    
    override fun getAllActivities(): Flow<List<Activity>> {
        return _activities.asStateFlow()
    }
    
    override fun getRecentActivities(limit: Int): Flow<List<Activity>> {
        return _activities.map { activities ->
            activities.take(limit)
        }
    }
    
    override suspend fun clearAllActivities() {
        _activities.value = emptyList()
        saveActivitiesToStorage()
    }
}