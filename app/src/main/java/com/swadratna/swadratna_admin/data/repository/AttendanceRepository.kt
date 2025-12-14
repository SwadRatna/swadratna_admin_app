package com.swadratna.swadratna_admin.data.repository

import com.swadratna.swadratna_admin.data.model.AttendanceResponse
import com.swadratna.swadratna_admin.data.wrapper.Result

interface AttendanceRepository {
    suspend fun getAttendance(
        dateFrom: String,
        dateTo: String,
        storeId: String,
        staffId: String? = null
    ): Result<AttendanceResponse>
}
