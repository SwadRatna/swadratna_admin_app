package com.swadratna.swadratna_admin.data.repository

import com.swadratna.swadratna_admin.data.model.SalesResponse
import com.swadratna.swadratna_admin.data.remote.api.SalesApi
import com.swadratna.swadratna_admin.data.wrapper.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

import com.swadratna.swadratna_admin.utils.NetworkErrorHandler

class SalesRepositoryImpl @Inject constructor(
    private val api: SalesApi
) : SalesRepository {
    override fun getSales(
        date: String?,
        fromDate: String?,
        toDate: String?,
        locationIds: String?
    ): Flow<Result<SalesResponse>> = flow {
        emit(Result.Loading)
        try {
            val response = api.getSales(date, fromDate, toDate, locationIds)
            if (response.isSuccessful && response.body() != null) {
                emit(Result.Success(response.body()!!))
            } else {
                emit(Result.Error(response.message() ?: "Unknown error"))
            }
        } catch (e: Exception) {
            if (e is java.util.concurrent.CancellationException) throw e
            emit(Result.Error(NetworkErrorHandler.getErrorMessage(e), e))
        }
    }
}
