package com.swadratna.swadratna_admin.data.repository

import com.swadratna.swadratna_admin.data.model.AttendanceResponse
import com.swadratna.swadratna_admin.data.remote.api.AttendanceApi
import com.swadratna.swadratna_admin.data.wrapper.Result
import com.swadratna.swadratna_admin.utils.NetworkErrorHandler
import javax.inject.Inject

class AttendanceRepositoryImpl @Inject constructor(
    private val api: AttendanceApi
) : AttendanceRepository {
    override suspend fun getAttendance(
        dateFrom: String,
        dateTo: String,
        storeId: String,
        staffId: String?
    ): Result<AttendanceResponse> {
        return try {
            val response = api.getAttendance(dateFrom, dateTo, storeId, staffId)
            Result.Success(response)
        } catch (e: Exception) {
            Result.Error(NetworkErrorHandler.getErrorMessage(e))
        }
    }
}
