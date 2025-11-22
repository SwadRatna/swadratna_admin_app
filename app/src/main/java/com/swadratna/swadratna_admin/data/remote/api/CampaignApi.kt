package com.swadratna.swadratna_admin.data.remote.api

import com.google.gson.annotations.SerializedName
import com.swadratna.swadratna_admin.data.model.Campaign
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.PATCH
import retrofit2.http.DELETE
import retrofit2.http.Path
import retrofit2.http.Query

interface CampaignApi {
    @GET("campaigns")
    suspend fun getCampaigns(): List<Campaign>

    @POST("campaigns")
    suspend fun createCampaign(@Body body: CreateCampaignRequest): Campaign

    // Admin APIs
    @POST("api/v1/admin/campaigns")
    suspend fun createAdminCampaign(@Body body: AdminCreateCampaignRequest): AdminCampaignResponse

    @GET("api/v1/admin/campaigns")
    suspend fun listAdminCampaigns(
        @Query("status") status: String? = null,
        @Query("type") type: String? = null,
        @Query("search") search: String? = null,
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null
    ): AdminCampaignListResponse

    @GET("api/v1/admin/campaigns/{id}")
    suspend fun getAdminCampaignDetails(@Path("id") id: Long): AdminCampaignResponse

    @PUT("api/v1/admin/campaigns/{id}")
    suspend fun updateAdminCampaign(
        @Path("id") id: Long,
        @Body body: AdminUpdateCampaignRequest
    ): AdminCampaignResponse

    @PATCH("api/v1/admin/campaigns/{id}/status")
    suspend fun updateAdminCampaignStatus(
        @Path("id") id: Long,
        @Body body: AdminUpdateCampaignStatusRequest
    ): AdminCampaignResponse

    @DELETE("api/v1/admin/campaigns/{id}")
    suspend fun deleteAdminCampaign(@Path("id") id: Long): AdminDeleteResponse

    // Public APIs
    @GET("api/v1/campaigns/active")
    suspend fun getActiveCampaigns(
        @Query("store_id") storeId: Long? = null,
        @Query("category_ids") categoryIdsCsv: String? = null
    ): AdminCampaignListResponse

    @POST("api/v1/campaigns/validate-promo")
    suspend fun validatePromo(@Body body: ValidatePromoRequest): ValidatePromoResponse
}

// Existing simple request
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

// Admin DTOs matching snake_case API
 data class AdminCreateCampaignRequest(
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("type") val type: String = "promotion",
    @SerializedName("start_date") val startDate: String,
    @SerializedName("end_date") val endDate: String,
    @SerializedName("target_franchises") val targetFranchises: List<Int> = emptyList(),
    @SerializedName("target_categories") val targetCategories: List<Int> = emptyList(),
    @SerializedName("image_url") val imageUrl: String? = null,
    @SerializedName("banner_image_url") val bannerImageUrl: String? = null,
    @SerializedName("discount_type") val discountType: String? = null,
    @SerializedName("discount_value") val discountValue: Int? = null,
    @SerializedName("min_order_amount") val minOrderAmount: Int? = null,
    @SerializedName("max_discount_amount") val maxDiscountAmount: Int? = null,
    @SerializedName("promo_code") val promoCode: String? = null,
    @SerializedName("promo_code_limit") val promoCodeLimit: Int? = null,
    @SerializedName("priority") val priority: Int? = null,
    @SerializedName("terms_conditions") val termsConditions: String? = null,
    @SerializedName("youtube_video_url") val youtubeVideoUrl: String? = null
)

 data class AdminUpdateCampaignRequest(
    @SerializedName("title") val title: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("start_date") val startDate: String? = null,
    @SerializedName("end_date") val endDate: String? = null,
    @SerializedName("target_franchises") val targetFranchises: List<Int>? = null,
    @SerializedName("target_categories") val targetCategories: List<Int>? = null,
    @SerializedName("image_url") val imageUrl: String? = null,
    @SerializedName("banner_image_url") val bannerImageUrl: String? = null,
    @SerializedName("discount_type") val discountType: String? = null,
    @SerializedName("discount_value") val discountValue: Int? = null,
    @SerializedName("min_order_amount") val minOrderAmount: Int? = null,
    @SerializedName("max_discount_amount") val maxDiscountAmount: Int? = null,
    @SerializedName("promo_code") val promoCode: String? = null,
    @SerializedName("promo_code_limit") val promoCodeLimit: Int? = null,
    @SerializedName("priority") val priority: Int? = null,
    @SerializedName("terms_conditions") val termsConditions: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("youtube_video_url") val youtubeVideoUrl: String? = null
)

 data class AdminUpdateCampaignStatusRequest(
    @SerializedName("status") val status: String
)

 data class AdminCampaignResponse(
    @SerializedName("id") val id: Long?,
    @SerializedName("title") val title: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("start_date") val startDate: String?,
    @SerializedName("end_date") val endDate: String?,
    @SerializedName("target_franchises") val targetFranchises: List<Int>?,
    @SerializedName("target_categories") val targetCategories: List<Int>?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("banner_image_url") val bannerImageUrl: String?,
    @SerializedName("discount_type") val discountType: String?,
    @SerializedName("discount_value") val discountValue: Int?,
    @SerializedName("min_order_amount") val minOrderAmount: Int?,
    @SerializedName("max_discount_amount") val maxDiscountAmount: Int?,
    @SerializedName("promo_code") val promoCode: String?,
    @SerializedName("promo_code_limit") val promoCodeLimit: Int?,
    @SerializedName("priority") val priority: Int?,
    @SerializedName("terms_conditions") val termsConditions: String?,
    @SerializedName("youtube_video_url") val youtubeVideoUrl: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?
 )

 data class AdminCampaignListResponse(
    @SerializedName("campaigns") val campaigns: List<AdminCampaignResponse> = emptyList(),
    @SerializedName("pagination") val pagination: Pagination? = null
 )

 data class Pagination(
    @SerializedName("has_next") val hasNext: Boolean? = null,
    @SerializedName("has_prev") val hasPrev: Boolean? = null,
    @SerializedName("limit") val limit: Int? = null,
    @SerializedName("page") val page: Int? = null,
    @SerializedName("total") val total: Int? = null,
    @SerializedName("total_pages") val totalPages: Int? = null
 )

 data class AdminDeleteResponse(
    @SerializedName("success") val success: Boolean = true,
    @SerializedName("message") val message: String? = null
)

 data class ValidatePromoRequest(
    @SerializedName("promo_code") val promoCode: String,
    @SerializedName("store_id") val storeId: Long
)

 data class ValidatePromoResponse(
    @SerializedName("valid") val valid: Boolean,
    @SerializedName("message") val message: String? = null,
    @SerializedName("discount_type") val discountType: String? = null,
    @SerializedName("discount_value") val discountValue: Int? = null,
    @SerializedName("max_discount_amount") val maxDiscountAmount: Int? = null
)