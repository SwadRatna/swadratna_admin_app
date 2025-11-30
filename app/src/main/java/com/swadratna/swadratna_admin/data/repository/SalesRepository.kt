package com.swadratna.swadratna_admin.data.repository

import com.swadratna.swadratna_admin.data.model.SalesResponse
import com.swadratna.swadratna_admin.data.wrapper.Result
import kotlinx.coroutines.flow.Flow

interface SalesRepository {
    fun getSales(
        date: String?,
        fromDate: String?,
        toDate: String?,
        locationIds: String?
    ): Flow<Result<SalesResponse>>
}
