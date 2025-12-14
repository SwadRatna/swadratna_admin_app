package com.swadratna.swadratna_admin.data.remote.api

import com.swadratna.swadratna_admin.data.model.AttendanceResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface AttendanceApi {
    @GET("api/v1/admin/attendance")
    suspend fun getAttendance(
        @Query("date_from") dateFrom: String,
        @Query("date_to") dateTo: String,
        @Query("store_id") storeId: String,
        @Query("staff_id") staffId: String? = null
    ): AttendanceResponse
}
