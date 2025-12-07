package com.swadratna.swadratna_admin.data.repository

import com.swadratna.swadratna_admin.data.model.WithdrawalResponse
import com.swadratna.swadratna_admin.data.model.WithdrawalStatusUpdateRequest
import com.swadratna.swadratna_admin.data.model.WithdrawalStatusUpdateResponse
import com.swadratna.swadratna_admin.data.wrapper.Result
import kotlinx.coroutines.flow.Flow

interface WithdrawalRepository {
    suspend fun getWithdrawals(status: String?, limit: Int?): Flow<Result<WithdrawalResponse>>
    suspend fun getPendingWithdrawals(): Flow<Result<WithdrawalResponse>>
    suspend fun updateWithdrawalStatus(id: Long, status: String, request: WithdrawalStatusUpdateRequest): Flow<Result<WithdrawalStatusUpdateResponse>>
}
