package com.swadratna.swadratna_admin.data.repository

import com.swadratna.swadratna_admin.data.model.Store
import com.swadratna.swadratna_admin.data.model.StoreRequest
import com.swadratna.swadratna_admin.data.model.StoreResponse
import com.swadratna.swadratna_admin.data.remote.api.StoreApiService
import com.swadratna.swadratna_admin.utils.NetworkErrorHandler
import javax.inject.Inject

class StoreRepositoryImpl @Inject constructor(
    private val storeApiService: StoreApiService
) : StoreRepository {
    
    override suspend fun getStores(page: Int, limit: Int, restaurantId: Int): Result<StoreResponse> {
        return try {
            val response = storeApiService.getStores(page, limit, restaurantId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(Exception(NetworkErrorHandler.getErrorMessage(e), e))
        }
    }
    
    override suspend fun createStore(storeRequest: StoreRequest): Result<Store> {
        return try {
            val response = storeApiService.createStore(storeRequest)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(Exception(NetworkErrorHandler.getErrorMessage(e), e))
        }
    }
    
    override suspend fun updateStore(storeId: Int, storeRequest: StoreRequest): Result<Store> {
        return try {
            val response = storeApiService.updateStore(storeId, storeRequest)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(Exception(NetworkErrorHandler.getErrorMessage(e), e))
        }
    }
    
    override suspend fun deleteStore(storeId: Int): Result<Unit> {
        return try {
            storeApiService.deleteStore(storeId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(NetworkErrorHandler.getErrorMessage(e), e))
        }
    }
}