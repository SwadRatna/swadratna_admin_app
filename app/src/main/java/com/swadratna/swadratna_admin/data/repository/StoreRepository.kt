package com.swadratna.swadratna_admin.data.repository

import com.swadratna.swadratna_admin.data.model.Store
import com.swadratna.swadratna_admin.data.model.StoreRequest
import com.swadratna.swadratna_admin.data.model.StoreResponse

interface StoreRepository {
    suspend fun getStores(page: Int, limit: Int, restaurantId: Int): Result<StoreResponse>
    suspend fun createStore(storeRequest: StoreRequest): Result<Store>
    suspend fun updateStore(storeId: Int, storeRequest: StoreRequest): Result<Store>
    suspend fun deleteStore(storeId: Int): Result<Unit>
}