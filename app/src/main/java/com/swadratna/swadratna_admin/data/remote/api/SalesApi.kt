package com.swadratna.swadratna_admin.data.remote.api

import com.swadratna.swadratna_admin.data.model.SalesResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface SalesApi {
    @GET("api/v1/admin/sales")
    suspend fun getSales(
        @Query("date") date: String?,
        @Query("from_date") fromDate: String?,
        @Query("to_date") toDate: String?,
        @Query("location_ids") locationIds: String?
    ): Response<SalesResponse>
}
