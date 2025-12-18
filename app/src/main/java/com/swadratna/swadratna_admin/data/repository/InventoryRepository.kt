package com.swadratna.swadratna_admin.data.repository

import com.swadratna.swadratna_admin.data.model.CreateIngredientRequest
import com.swadratna.swadratna_admin.data.model.Ingredient
import com.swadratna.swadratna_admin.data.remote.IngredientOperationResponse
import com.swadratna.swadratna_admin.data.remote.api.InventoryApi
import com.swadratna.swadratna_admin.data.remote.toDomain
import com.swadratna.swadratna_admin.data.remote.toDto
import com.swadratna.swadratna_admin.data.remote.UpdateIngredientDto
import com.swadratna.swadratna_admin.data.model.StockInRequest
import com.swadratna.swadratna_admin.data.model.StockOutRequest
import com.swadratna.swadratna_admin.data.model.WastageRequest
import com.swadratna.swadratna_admin.data.model.AdjustmentRequest
import com.swadratna.swadratna_admin.data.remote.StockOperationResponse
import com.swadratna.swadratna_admin.utils.NetworkErrorHandler
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class InventoryRepository @Inject constructor(
    private val api: InventoryApi
) {
    suspend fun getIngredients(locationId: Int?): Result<List<Ingredient>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getIngredients(locationId)
            Result.success((response.ingredients ?: emptyList()).map { it.toDomain() })
        } catch (e: Throwable) {
            Result.failure(Exception(NetworkErrorHandler.getErrorMessage(e), e))
        }
    }

    suspend fun createIngredient(
        request: CreateIngredientRequest,
        locationId: Int
    ): Result<IngredientOperationResponse> = withContext(Dispatchers.IO) {
        try {
            Result.success(api.createIngredient(request.toDto(locationId)))
        } catch (e: Throwable) {
            Result.failure(Exception(NetworkErrorHandler.getErrorMessage(e), e))
        }
    }

    suspend fun updateIngredient(
        id: Int,
        reorderLevel: Int,
        costPerUnit: Double
    ): Result<IngredientOperationResponse> = withContext(Dispatchers.IO) {
        try {
            val dto = UpdateIngredientDto(reorderLevel = reorderLevel, costPerUnit = costPerUnit)
            Result.success(api.updateIngredient(id, dto))
        } catch (e: Throwable) {
            Result.failure(Exception(NetworkErrorHandler.getErrorMessage(e), e))
        }
    }

    suspend fun deleteIngredient(
        id: Int
    ): Result<IngredientOperationResponse> = withContext(Dispatchers.IO) {
        try {
            Result.success(api.deleteIngredient(id))
        } catch (e: Throwable) {
            Result.failure(Exception(NetworkErrorHandler.getErrorMessage(e), e))
        }
    }

    suspend fun stockIn(request: StockInRequest): Result<StockOperationResponse> = withContext(Dispatchers.IO) {
        try {
            Result.success(api.stockIn(request.toDto()))
        } catch (e: Throwable) {
            Result.failure(Exception(NetworkErrorHandler.getErrorMessage(e), e))
        }
    }

    suspend fun stockOut(request: StockOutRequest): Result<StockOperationResponse> = withContext(Dispatchers.IO) {
        try {
            Result.success(api.stockOut(request.toDto()))
        } catch (e: Throwable) {
            Result.failure(Exception(NetworkErrorHandler.getErrorMessage(e), e))
        }
    }

    suspend fun stockWastage(request: WastageRequest): Result<StockOperationResponse> = withContext(Dispatchers.IO) {
        try {
            Result.success(api.stockWastage(request.toDto()))
        } catch (e: Throwable) {
            Result.failure(Exception(NetworkErrorHandler.getErrorMessage(e), e))
        }
    }

    suspend fun stockAdjustment(request: AdjustmentRequest): Result<StockOperationResponse> = withContext(Dispatchers.IO) {
        try {
            Result.success(api.stockAdjustment(request.toDto()))
        } catch (e: Throwable) {
            Result.failure(Exception(NetworkErrorHandler.getErrorMessage(e), e))
        }
    }
}
