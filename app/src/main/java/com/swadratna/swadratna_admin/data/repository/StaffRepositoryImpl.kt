package com.swadratna.swadratna_admin.data.repository

import com.swadratna.swadratna_admin.data.model.CreateStaffRequest
import com.swadratna.swadratna_admin.data.model.StaffOperationResponse
import com.swadratna.swadratna_admin.data.model.StaffResponse
import com.swadratna.swadratna_admin.data.model.UpdateStaffRequest
import com.swadratna.swadratna_admin.data.remote.api.StaffApiService
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StaffRepositoryImpl @Inject constructor(
    private val staffApiService: StaffApiService
) : StaffRepository {
    
    override suspend fun getStaff(storeId: Int): Result<StaffResponse> {
        return try {
            val response = staffApiService.getStaff(storeId)
            Result.success(response)
        } catch (e: HttpException) {
            val errorMessage = when (e.code()) {
                500 -> "Server error: Failed to load staff data. Please try again later."
                404 -> "Store not found or no staff data available."
                401 -> "Authentication failed. Please login again."
                403 -> "Access denied. You don't have permission to view this data."
                else -> "Failed to load staff data. Error code: ${e.code()}"
            }
            Result.failure(Exception(errorMessage))
        } catch (e: IOException) {
            Result.failure(Exception("Network error. Please check your internet connection."))
        } catch (e: Exception) {
            Result.failure(Exception("An unexpected error occurred: ${e.message}"))
        }
    }
    
    override suspend fun createStaff(request: CreateStaffRequest): Result<StaffOperationResponse> {
        return try {
            val response = staffApiService.createStaff(request)
            Result.success(response)
        } catch (e: HttpException) {
            val errorMessage = when (e.code()) {
                400 -> "Invalid staff data. Please check all required fields."
                401 -> "Authentication failed. Please login again."
                403 -> "Access denied. You don't have permission to create staff."
                409 -> "Staff with this email already exists."
                422 -> "Validation failed. Please check your input data."
                500 -> "Server error: Failed to create staff. Please try again later."
                else -> "Failed to create staff. Error code: ${e.code()}"
            }
            Result.failure(Exception(errorMessage))
        } catch (e: IOException) {
            Result.failure(Exception("Network error. Please check your internet connection."))
        } catch (e: Exception) {
            Result.failure(Exception("An unexpected error occurred: ${e.message}"))
        }
    }
    
    override suspend fun updateStaff(staffId: Int, request: UpdateStaffRequest): Result<StaffOperationResponse> {
        return try {
            val response = staffApiService.updateStaff(staffId, request)
            Result.success(response)
        } catch (e: HttpException) {
            val errorMessage = when (e.code()) {
                400 -> "Invalid staff data. Please check all required fields."
                401 -> "Authentication failed. Please login again."
                403 -> "Access denied. You don't have permission to update staff."
                404 -> "Staff member not found."
                409 -> "Staff with this email already exists."
                422 -> "Validation failed. Please check your input data."
                500 -> "Server error: Failed to update staff. Please try again later."
                else -> "Failed to update staff. Error code: ${e.code()}"
            }
            Result.failure(Exception(errorMessage))
        } catch (e: IOException) {
            Result.failure(Exception("Network error. Please check your internet connection."))
        } catch (e: Exception) {
            Result.failure(Exception("An unexpected error occurred: ${e.message}"))
        }
    }
    
    override suspend fun deleteStaff(staffId: Int): Result<StaffOperationResponse> {
        return try {
            val response = staffApiService.deleteStaff(staffId)
            Result.success(response)
        } catch (e: HttpException) {
            val errorMessage = when (e.code()) {
                401 -> "Authentication failed. Please login again."
                403 -> "Access denied. You don't have permission to delete staff."
                404 -> "Staff member not found."
                500 -> "Server error: Failed to delete staff. Please try again later."
                else -> "Failed to delete staff. Error code: ${e.code()}"
            }
            Result.failure(Exception(errorMessage))
        } catch (e: IOException) {
            Result.failure(Exception("Network error. Please check your internet connection."))
        } catch (e: Exception) {
            Result.failure(Exception("An unexpected error occurred: ${e.message}"))
        }
    }
}