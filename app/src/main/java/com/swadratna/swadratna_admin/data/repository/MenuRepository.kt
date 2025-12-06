package com.swadratna.swadratna_admin.data.repository

import android.util.Log
import com.swadratna.swadratna_admin.data.model.MenuItem
import com.swadratna.swadratna_admin.data.model.MenuCategory
import com.swadratna.swadratna_admin.data.model.CreateMenuItemRequest
import com.swadratna.swadratna_admin.data.model.UpdateMenuItemRequest
import com.swadratna.swadratna_admin.data.model.ToggleAvailabilityRequest
import com.swadratna.swadratna_admin.data.remote.CreateMenuCategoryDto
import com.swadratna.swadratna_admin.data.remote.MenuCategoryResponse
import com.swadratna.swadratna_admin.data.remote.MenuItemResponse
import com.swadratna.swadratna_admin.data.remote.MenuItemsListResponse
import com.swadratna.swadratna_admin.data.remote.api.MenuApi
import com.swadratna.swadratna_admin.data.remote.toDomain
import com.swadratna.swadratna_admin.data.remote.toCreateDto
import com.swadratna.swadratna_admin.data.remote.toDto
import com.swadratna.swadratna_admin.data.remote.toUpdateDto
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import com.swadratna.swadratna_admin.utils.NetworkErrorHandler

class MenuRepository @Inject constructor(
    private val api: MenuApi
) {

    suspend fun getMenu(categoryId: Int? = null): Result<List<MenuItem>> = withContext(Dispatchers.IO) {
        try {
            Result.success(api.getMenu(categoryId?.toString()).map { it.toDomain() })
        } catch (e: Throwable) {
            Result.failure(Exception(NetworkErrorHandler.getErrorMessage(e), e))
        }
    }

    suspend fun getMenuCategories(): Result<List<MenuCategory>> = withContext(Dispatchers.IO) {
        try {
            Result.success(api.getMenuCategories().map { it.toDomain() })
        } catch (e: Throwable) {
            Result.failure(Exception(NetworkErrorHandler.getErrorMessage(e), e))
        }
    }

    suspend fun createMenuCategory(category: MenuCategory): Result<MenuCategoryResponse> = withContext(Dispatchers.IO) {
        try {
            Result.success(api.createMenuCategory(category.toCreateDto()))
        } catch (e: Throwable) {
            Result.failure(Exception(NetworkErrorHandler.getErrorMessage(e), e))
        }
    }

    suspend fun toggleCategoryAvailability(id: Int, availability: ToggleAvailabilityRequest): Result<MenuCategoryResponse> = withContext(Dispatchers.IO) {
        try {
            Result.success(api.toggleCategoryAvailability(id, availability.toDto()))
        } catch (e: Throwable) {
            Result.failure(Exception(NetworkErrorHandler.getErrorMessage(e), e))
        }
    }

    suspend fun deleteCategory(id: Int): Result<MenuCategoryResponse> = withContext(Dispatchers.IO) {
        try {
            Result.success(api.deleteCategory(id))
        } catch (e: Throwable) {
            Result.failure(Exception(NetworkErrorHandler.getErrorMessage(e), e))
        }
    }

    // Menu Items methods
    suspend fun getMenuItems(
        categoryId: Int? = null,
        isAvailable: Boolean? = null,
        search: String? = null,
        page: Int = 1,
        limit: Int = 20
    ): Result<MenuItemsListResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.getMenuItems(categoryId, isAvailable, search, page, limit)
            Result.success(response)
        } catch (e: Throwable) {
            Log.e("MenuRepository", "API call failed with error: ${e.message}", e)
            Result.failure(Exception(NetworkErrorHandler.getErrorMessage(e), e))
        }
    }

    suspend fun createMenuItem(menuItem: CreateMenuItemRequest): Result<MenuItemResponse> = withContext(Dispatchers.IO) {
        val dto = menuItem.toDto()
        try {
            val response = api.createMenuItem(dto)
            Result.success(response)
        } catch (e: Throwable) {
            Result.failure(Exception(NetworkErrorHandler.getErrorMessage(e), e))
        }
    }

    suspend fun updateMenuItem(id: Int, menuItem: UpdateMenuItemRequest): Result<MenuItemResponse> = withContext(Dispatchers.IO) {
        try {
            Result.success(api.updateMenuItem(id, menuItem.toDto()))
        } catch (e: Throwable) {
            Result.failure(Exception(NetworkErrorHandler.getErrorMessage(e), e))
        }
    }

    suspend fun toggleMenuItemAvailability(id: Int, availability: ToggleAvailabilityRequest): Result<MenuItemResponse> = withContext(Dispatchers.IO) {
        try {
            Result.success(api.toggleMenuItemAvailability(id, availability.toDto()))
        } catch (e: Throwable) {
            Result.failure(Exception(NetworkErrorHandler.getErrorMessage(e), e))
        }
    }

    suspend fun deleteMenuItem(id: Int): Result<MenuItemResponse> = withContext(Dispatchers.IO) {
        try {
            Result.success(api.deleteMenuItem(id))
        } catch (e: Throwable) {
            Result.failure(Exception(NetworkErrorHandler.getErrorMessage(e), e))
        }
    }

    suspend fun updateCategory(id: Int, category: MenuCategory): Result<MenuCategoryResponse> = withContext(Dispatchers.IO) {
        try {
            Result.success(api.updateMenuCategory(id, category.toUpdateDto()))
        } catch (e: Throwable) {
            Result.failure(Exception(NetworkErrorHandler.getErrorMessage(e), e))
        }
    }
}
