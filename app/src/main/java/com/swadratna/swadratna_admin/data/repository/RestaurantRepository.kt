package com.swadratna.swadratna_admin.data.repository

import com.swadratna.swadratna_admin.data.model.RestaurantProfileRequest
import com.swadratna.swadratna_admin.data.wrapper.Result

interface RestaurantRepository {
    suspend fun updateRestaurantProfile(restaurantId: Int, request: RestaurantProfileRequest): Result<Unit>
}
