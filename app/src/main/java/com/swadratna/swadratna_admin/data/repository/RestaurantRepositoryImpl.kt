package com.swadratna.swadratna_admin.data.repository

import com.swadratna.swadratna_admin.data.model.RestaurantProfileRequest
import com.swadratna.swadratna_admin.data.remote.api.RestaurantApiService
import com.swadratna.swadratna_admin.data.wrapper.Result
import com.swadratna.swadratna_admin.utils.NetworkErrorHandler
import javax.inject.Inject

class RestaurantRepositoryImpl @Inject constructor(
    private val api: RestaurantApiService
) : RestaurantRepository {
    override suspend fun updateRestaurantProfile(restaurantId: Int, request: RestaurantProfileRequest): Result<Unit> {
        return try {
            val response = api.updateRestaurantProfile(restaurantId, request)
            if (response.isSuccessful) {
                Result.Success(Unit)
            } else {
                Result.Error(Exception("Failed to update profile: ${response.code()}").toString())
            }
        } catch (e: Exception) {
            Result.Error(Exception(NetworkErrorHandler.getErrorMessage(e), e).toString())
        }
    }
}
