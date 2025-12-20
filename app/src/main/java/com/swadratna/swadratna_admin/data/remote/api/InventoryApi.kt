package com.swadratna.swadratna_admin.data.remote.api

import com.swadratna.swadratna_admin.data.remote.CreateIngredientDto
import com.swadratna.swadratna_admin.data.remote.IngredientOperationResponse
import com.swadratna.swadratna_admin.data.remote.IngredientsResponse
import com.swadratna.swadratna_admin.data.remote.UpdateIngredientDto
import com.swadratna.swadratna_admin.data.remote.StockOperationResponse
import com.swadratna.swadratna_admin.data.remote.StockInRequestDto
import com.swadratna.swadratna_admin.data.remote.StockOutRequestDto
import com.swadratna.swadratna_admin.data.remote.WastageRequestDto
import com.swadratna.swadratna_admin.data.remote.AdjustmentRequestDto
import com.swadratna.swadratna_admin.data.remote.LowStockResponse
import com.swadratna.swadratna_admin.data.remote.InventoryMovementsResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PATCH
import retrofit2.http.DELETE
import retrofit2.http.Query
import retrofit2.http.Path

interface InventoryApi {
    @GET("api/v1/admin/inventory/ingredients")
    suspend fun getIngredients(
        @Query("location_id") locationId: Int? = null
    ): IngredientsResponse

    @POST("api/v1/admin/inventory/ingredients")
    suspend fun createIngredient(
        @Body ingredient: CreateIngredientDto
    ): IngredientOperationResponse

    @PATCH("api/v1/admin/inventory/ingredients/{id}")
    suspend fun updateIngredient(
        @Path("id") id: Int,
        @Body update: UpdateIngredientDto
    ): IngredientOperationResponse

    @DELETE("api/v1/admin/inventory/ingredients/{id}")
    suspend fun deleteIngredient(
        @Path("id") id: Int
    ): IngredientOperationResponse

    @POST("api/v1/admin/inventory/stock-in")
    suspend fun stockIn(
        @Body body: StockInRequestDto
    ): StockOperationResponse

    @POST("api/v1/admin/inventory/stock-out")
    suspend fun stockOut(
        @Body body: StockOutRequestDto
    ): StockOperationResponse

    @POST("api/v1/admin/inventory/wastage")
    suspend fun stockWastage(
        @Body body: WastageRequestDto
    ): StockOperationResponse

    @POST("api/v1/admin/inventory/adjustment")
    suspend fun stockAdjustment(
        @Body body: AdjustmentRequestDto
    ): StockOperationResponse

    @GET("api/v1/admin/inventory/low-stock")
    suspend fun getLowStock(): LowStockResponse

    @GET("api/v1/admin/inventory/movements")
    suspend fun getMovements(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 200
    ): InventoryMovementsResponse
}
