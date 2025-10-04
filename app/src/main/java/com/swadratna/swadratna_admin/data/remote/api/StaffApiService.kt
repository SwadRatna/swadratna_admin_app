package com.swadratna.swadratna_admin.data.remote.api

import com.swadratna.swadratna_admin.data.model.CreateStaffRequest
import com.swadratna.swadratna_admin.data.model.StaffOperationResponse
import com.swadratna.swadratna_admin.data.model.StaffResponse
import com.swadratna.swadratna_admin.data.model.UpdateStaffRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface StaffApiService {
    @GET("api/v1/admin/staff")
    suspend fun getStaff(
        @Query("store_id") storeId: Int
    ): StaffResponse
    
    @POST("api/v1/admin/staff")
    suspend fun createStaff(
        @Body request: CreateStaffRequest
    ): StaffOperationResponse
    
    @PUT("api/v1/admin/staff/{staffId}")
    suspend fun updateStaff(
        @Path("staffId") staffId: Int,
        @Body request: UpdateStaffRequest
    ): StaffOperationResponse
    
    @DELETE("api/v1/admin/staff/{staffId}")
    suspend fun deleteStaff(
        @Path("staffId") staffId: Int
    )
}