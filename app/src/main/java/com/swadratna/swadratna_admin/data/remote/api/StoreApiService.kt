package com.swadratna.swadratna_admin.data.remote.api

import com.swadratna.swadratna_admin.data.model.Store
import com.swadratna.swadratna_admin.data.model.StoreRequest
import com.swadratna.swadratna_admin.data.model.StoreResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface StoreApiService {
    @GET("api/v1/admin/stores")
    suspend fun getStores(
        @Query("page") page: Int,
        @Query("limit") limit: Int,
        @Query("restaurant_id") restaurantId: Int
    ): StoreResponse
    
    @POST("api/v1/admin/stores")
    suspend fun createStore(
        @Body storeRequest: StoreRequest
    ): Store
    
    @PUT("api/v1/admin/stores/{storeId}")
    suspend fun updateStore(
        @Path("storeId") storeId: Int,
        @Body storeRequest: StoreRequest
    ): Store
    
    @DELETE("api/v1/admin/stores/{storeId}")
    suspend fun deleteStore(
        @Path("storeId") storeId: Int
    )
}