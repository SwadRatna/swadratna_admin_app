package com.swadratna.swadratna_admin.data.repository

import com.swadratna.swadratna_admin.data.model.WithdrawalResponse
import com.swadratna.swadratna_admin.data.model.WithdrawalStatusUpdateRequest
import com.swadratna.swadratna_admin.data.model.WithdrawalStatusUpdateResponse
import com.swadratna.swadratna_admin.data.remote.api.WithdrawalApi
import com.swadratna.swadratna_admin.data.wrapper.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class WithdrawalRepositoryImpl @Inject constructor(
    private val api: WithdrawalApi
) : WithdrawalRepository {

    override suspend fun getWithdrawals(status: String?, limit: Int?): Flow<Result<WithdrawalResponse>> = flow {
        emit(Result.Loading)
        try {
            val response = api.getWithdrawals(status, limit)
            emit(Result.Success(response))
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Unknown error", e))
        }
    }

    override suspend fun getPendingWithdrawals(): Flow<Result<WithdrawalResponse>> = flow {
        emit(Result.Loading)
        try {
            val response = api.getPendingWithdrawals()
            emit(Result.Success(response))
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Unknown error", e))
        }
    }

    override suspend fun updateWithdrawalStatus(id: Long, status: String, request: WithdrawalStatusUpdateRequest): Flow<Result<WithdrawalStatusUpdateResponse>> = flow {
        emit(Result.Loading)
        try {
            val response = api.updateWithdrawalStatus(id, status, request)
            emit(Result.Success(response))
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Unknown error", e))
        }
    }
}
