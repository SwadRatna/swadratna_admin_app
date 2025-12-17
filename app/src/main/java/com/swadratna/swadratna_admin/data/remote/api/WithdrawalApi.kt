package com.swadratna.swadratna_admin.data.remote.api

import com.swadratna.swadratna_admin.data.model.WithdrawalResponse
import com.swadratna.swadratna_admin.data.model.WithdrawalStatusUpdateRequest
import com.swadratna.swadratna_admin.data.model.WithdrawalStatusUpdateResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface WithdrawalApi {

    @GET("api/v1/admin/withdrawals")
    suspend fun getWithdrawals(
        @Query("status") status: String? = null,
        @Query("limit") limit: Int? = 50
    ): WithdrawalResponse

    @GET("api/v1/admin/withdrawals/pending")
    suspend fun getPendingWithdrawals(): WithdrawalResponse

    @POST("api/v1/admin/withdrawals/{id}/{status}")
    suspend fun updateWithdrawalStatus(
        @Path("id") id: Long,
        @Path("status") status: String,
        @Body request: WithdrawalStatusUpdateRequest
    ): WithdrawalStatusUpdateResponse
}
