package com.swadratna.swadratna_admin.data.repository

import com.swadratna.swadratna_admin.data.model.CreateStaffRequest
import com.swadratna.swadratna_admin.data.model.Staff
import com.swadratna.swadratna_admin.data.model.StaffOperationResponse
import com.swadratna.swadratna_admin.data.model.StaffResponse
import com.swadratna.swadratna_admin.data.model.UpdateStaffRequest

interface StaffRepository {
    suspend fun getStaff(storeId: Int): Result<StaffResponse>
    suspend fun createStaff(request: CreateStaffRequest): Result<StaffOperationResponse>
    suspend fun updateStaff(staffId: Int, request: UpdateStaffRequest): Result<StaffOperationResponse>
    suspend fun deleteStaff(staffId: Int): Result<StaffOperationResponse>
}