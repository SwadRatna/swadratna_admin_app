package com.swadratna.swadratna_admin.data.remote.api

import com.swadratna.swadratna_admin.data.model.Campaign
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface CampaignApi {
    @GET("campaigns")
    suspend fun getCampaigns(): List<Campaign>

    @POST("campaigns")
    suspend fun createCampaign(@Body body: CreateCampaignRequest): Campaign
}

data class CreateCampaignRequest(
    val title: String,
    val description: String,
    val startDate: String,
    val endDate: String,
    val type: String,
    val discount: Int?,
    val targetFranchises: String,
    val menuCategories: List<String>,
    val imageUrl: String?
)