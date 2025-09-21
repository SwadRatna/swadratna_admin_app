package com.swadratna.swadratna_admin.utils

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.reflect.TypeToken
import java.time.LocalDate
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit
import com.swadratna.swadratna_admin.data.model.Campaign
import com.swadratna.swadratna_admin.data.model.Store

@Singleton
@RequiresApi(Build.VERSION_CODES.O)
class SharedPrefsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )

    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(LocalDate::class.java, object : JsonSerializer<LocalDate> {
            override fun serialize(
                src: LocalDate?,
                typeOfSrc: java.lang.reflect.Type?,
                context: JsonSerializationContext?
            ): JsonElement {
                return JsonPrimitive(src?.toString())
            }
        })
        .registerTypeAdapter(LocalDate::class.java, object : JsonDeserializer<LocalDate> {
            override fun deserialize(
                json: JsonElement?,
                typeOfT: java.lang.reflect.Type?,
                context: JsonDeserializationContext?
            ): LocalDate {
                return LocalDate.parse(json?.asString)
            }
        })
        .create()

    fun saveStores(stores: List<Store>) {
        val storesJson = gson.toJson(stores)
        sharedPreferences.edit { putString(KEY_STORES, storesJson) }
    }

    fun getStores(): List<Store> {
        val storesJson = sharedPreferences.getString(KEY_STORES, null) ?: return emptyList()
        val type = object : TypeToken<List<Store>>() {}.type
        return gson.fromJson(storesJson, type)
    }

    fun saveCampaigns(campaigns: List<Campaign>) {
        val campaignsJson = gson.toJson(campaigns)
        sharedPreferences.edit { putString(KEY_CAMPAIGNS, campaignsJson) }
    }

    fun getCampaigns(): List<Campaign> {
        val campaignsJson = sharedPreferences.getString(KEY_CAMPAIGNS, null) ?: return emptyList()
        val type = object : TypeToken<List<Campaign>>() {}.type
        return gson.fromJson(campaignsJson, type)
    }

    companion object {
        private const val PREFS_NAME = "swadratna_admin_prefs"
        private const val KEY_STORES = "stores"
        private const val KEY_CAMPAIGNS = "campaigns"
    }
}