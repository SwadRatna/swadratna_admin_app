package com.swadratna.swadratna_admin.data.remote.api

import com.swadratna.swadratna_admin.data.model.RestaurantProfileRequest
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.PUT
import retrofit2.http.Path

interface RestaurantApiService {
    @PUT("api/v1/admin/restaurants/{restaurantId}")
    suspend fun updateRestaurantProfile(
        @Path("restaurantId") restaurantId: Int,
        @Body request: RestaurantProfileRequest
    ): Response<ResponseBody>
}
