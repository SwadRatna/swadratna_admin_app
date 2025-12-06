package com.swadratna.swadratna_admin.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.swadratna.swadratna_admin.data.model.Campaign
import com.swadratna.swadratna_admin.data.model.CampaignStatus
import com.swadratna.swadratna_admin.data.model.CampaignType
import com.swadratna.swadratna_admin.data.remote.api.CampaignApi
import com.swadratna.swadratna_admin.data.remote.api.CreateCampaignRequest
import com.swadratna.swadratna_admin.data.remote.api.AdminCreateCampaignRequest
import com.swadratna.swadratna_admin.data.remote.api.AdminUpdateCampaignRequest
import com.swadratna.swadratna_admin.data.remote.api.AdminUpdateCampaignStatusRequest
import com.swadratna.swadratna_admin.data.remote.api.AdminCampaignResponse
import com.swadratna.swadratna_admin.data.remote.api.AdminCampaignListResponse
import com.swadratna.swadratna_admin.data.remote.api.ValidatePromoRequest
import com.swadratna.swadratna_admin.data.remote.api.ValidatePromoResponse
import com.swadratna.swadratna_admin.data.wrapper.Result
import com.swadratna.swadratna_admin.utils.NetworkErrorHandler
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.IOException
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CampaignRepository @Inject constructor(
    private val api: CampaignApi,
    private val io: CoroutineDispatcher
) {

    suspend fun getCampaigns(): Result<List<Campaign>> = withContext(io) {
        try {
            Result.Success(api.getCampaigns())
        } catch (e: Throwable) {
            Result.Error(NetworkErrorHandler.getErrorMessage(e, "Failed to load campaigns"), e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun createCampaign(req: CreateCampaignRequest): Result<Campaign> = withContext(io) {
        try {
            Result.Success(api.createCampaign(req))
        } catch (e: Throwable) {
            Result.Error(NetworkErrorHandler.getErrorMessage(e, "Failed to create campaign"), e)
        }
    }

    // Admin APIs
    suspend fun adminCreateCampaign(req: AdminCreateCampaignRequest): Result<AdminCampaignResponse> = withContext(io) {
        try {
            Result.Success(api.createAdminCampaign(req))
        } catch (e: Throwable) {
            Result.Error(NetworkErrorHandler.getErrorMessage(e, "Failed to create campaign"), e)
        }
    }

    suspend fun adminListCampaigns(
        status: String? = null,
        type: String? = null,
        search: String? = null,
        page: Int? = null,
        limit: Int? = null
    ): Result<AdminCampaignListResponse> = withContext(io) {
        try {
            Result.Success(api.listAdminCampaigns(status, type, search, page, limit))
        } catch (e: Throwable) {
            Result.Error(NetworkErrorHandler.getErrorMessage(e, "Failed to load campaigns"), e)
        }
    }

    suspend fun adminGetCampaignDetails(id: Long): Result<AdminCampaignResponse> = withContext(io) {
        try {
            val response = api.getAdminCampaignDetails(id)
            android.util.Log.d("CampaignRepository", "Raw API response for campaign $id: youtubeVideoUrl = ${response.youtubeVideoUrl}")
            Result.Success(response)
        } catch (e: Throwable) {
            Result.Error(NetworkErrorHandler.getErrorMessage(e, "Failed to get details"), e)
        }
    }

    suspend fun adminUpdateCampaign(id: Long, req: AdminUpdateCampaignRequest): Result<AdminCampaignResponse> = withContext(io) {
        try {
            Result.Success(api.updateAdminCampaign(id, req))
        } catch (e: Throwable) {
            Result.Error(NetworkErrorHandler.getErrorMessage(e, "Failed to update campaign"), e)
        }
    }

    suspend fun adminUpdateCampaignStatus(id: Long, status: String): Result<AdminCampaignResponse> = withContext(io) {
        try {
            Result.Success(api.updateAdminCampaignStatus(id, AdminUpdateCampaignStatusRequest(status)))
        } catch (e: Throwable) {
            Result.Error(NetworkErrorHandler.getErrorMessage(e, "Failed to update status"), e)
        }
    }

    suspend fun adminDeleteCampaign(id: Long): Result<Boolean> = withContext(io) {
        try {
            val res = api.deleteAdminCampaign(id)
            Result.Success(res.success)
        } catch (e: Throwable) {
            Result.Error(NetworkErrorHandler.getErrorMessage(e, "Failed to delete campaign"), e)
        }
    }

    // Public APIs
    suspend fun getActiveCampaigns(storeId: Long? = null, categoryIdsCsv: String? = null): Result<AdminCampaignListResponse> = withContext(io) {
        try {
            Result.Success(api.getActiveCampaigns(storeId, categoryIdsCsv))
        } catch (e: Throwable) {
            Result.Error(NetworkErrorHandler.getErrorMessage(e, "Failed to load active campaigns"), e)
        }
    }

    suspend fun validatePromo(promoCode: String, storeId: Long): Result<ValidatePromoResponse> = withContext(io) {
        try {
            Result.Success(api.validatePromo(ValidatePromoRequest(promoCode, storeId)))
        } catch (e: Throwable) {
            Result.Error(NetworkErrorHandler.getErrorMessage(e, "Failed to validate promo"), e)
        }
    }
}
