package com.swadratna.swadratna_admin.data.repository

import com.swadratna.swadratna_admin.data.model.SalesResponse
import com.swadratna.swadratna_admin.data.model.SaleDto
import com.swadratna.swadratna_admin.data.model.Summary
import com.swadratna.swadratna_admin.data.model.Pagination
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
            val allSales = mutableListOf<SaleDto>()
            var totalAmount = 0.0
            var page = 1
            val limit = 200
            var hasNext = true

            while (hasNext) {
                val resp = api.getSales(date, fromDate, toDate, locationIds, page, limit)
                if (!resp.isSuccessful || resp.body() == null) {
                    emit(Result.Error(resp.message() ?: "Unknown error"))
                    return@flow
                }
                val body = resp.body()!!
                val sales = body.sales ?: emptyList()
                allSales.addAll(sales)
                totalAmount += sales.sumOf { it.amount ?: 0.0 }
                hasNext = body.pagination?.hasNext == true
                page += 1
                if (body.pagination?.totalPages != null && page > body.pagination.totalPages!!) {
                    hasNext = false
                }
            }

            val aggregated = SalesResponse(
                pagination = Pagination(
                    currentPage = 1,
                    hasNext = false,
                    hasPrev = false,
                    perPage = allSales.size,
                    totalCount = allSales.size,
                    totalPages = 1
                ),
                sales = allSales,
                summary = Summary(count = allSales.size, totalAmount = totalAmount)
            )
            emit(Result.Success(aggregated))
        } catch (e: Exception) {
            if (e is java.util.concurrent.CancellationException) throw e
            emit(Result.Error(NetworkErrorHandler.getErrorMessage(e), e))
        }
    }
}
