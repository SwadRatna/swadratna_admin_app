package com.swadratna.swadratna_admin.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.swadratna.swadratna_admin.model.Campaign
import com.swadratna.swadratna_admin.model.Store
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedPrefsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )
    private val gson = Gson()

    // Store methods
    fun saveStores(stores: List<Store>) {
        val storesJson = gson.toJson(stores)
        sharedPreferences.edit().putString(KEY_STORES, storesJson).apply()
    }

    fun getStores(): List<Store> {
        val storesJson = sharedPreferences.getString(KEY_STORES, null) ?: return emptyList()
        val type = object : TypeToken<List<Store>>() {}.type
        return gson.fromJson(storesJson, type)
    }

    // Campaign methods
    fun saveCampaigns(campaigns: List<Campaign>) {
        val campaignsJson = gson.toJson(campaigns)
        sharedPreferences.edit().putString(KEY_CAMPAIGNS, campaignsJson).apply()
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