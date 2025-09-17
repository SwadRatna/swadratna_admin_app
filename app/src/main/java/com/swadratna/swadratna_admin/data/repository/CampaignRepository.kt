package com.swadratna.swadratna_admin.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.swadratna.swadratna_admin.data.model.Campaign
import com.swadratna.swadratna_admin.data.model.CampaignStatus
import com.swadratna.swadratna_admin.data.model.CampaignType
import com.swadratna.swadratna_admin.data.remote.api.CampaignApi
import com.swadratna.swadratna_admin.data.remote.api.CreateCampaignRequest
import com.swadratna.swadratna_admin.data.wrapper.Result
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
            if (e is IOException) {
                Result.Success(emptyList())
            } else {
                Result.Error(e.message ?: "Unknown error", e)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun createCampaign(req: CreateCampaignRequest): Result<Campaign> = withContext(io) {
        try {
            Result.Success(api.createCampaign(req))
        } catch (e: Throwable) {
            if (e is IOException) {
                // offline: synthesize a local draft to keep UI functional
                val draft = Campaign(
                    id = "local-${System.currentTimeMillis()}",
                    title = req.title,
                    description = req.description,
                    startDate = LocalDate.parse(req.startDate),
                    endDate = LocalDate.parse(req.endDate),
                    status = CampaignStatus.DRAFT,
                    type = runCatching { CampaignType.valueOf(req.type) }.getOrDefault(CampaignType.DISCOUNT),
                    discount = req.discount ?: 0,
                    storeCount = 0,
                    imageUrl = req.imageUrl
                )
                Result.Success(draft)
            } else {
                Result.Error(e.message ?: "Unknown error", e)
            }
        }
    }
}
