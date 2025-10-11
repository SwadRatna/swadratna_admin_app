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
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MenuRepository @Inject constructor(
    private val api: MenuApi
) {

    suspend fun getMenu(categoryId: Int? = null): Result<List<MenuItem>> = withContext(Dispatchers.IO) {
        runCatching {
            api.getMenu(categoryId?.toString()).map { it.toDomain() }
        }
    }

    suspend fun getMenuCategories(): Result<List<MenuCategory>> = withContext(Dispatchers.IO) {
        runCatching {
            api.getMenuCategories().map { it.toDomain() }
        }
    }

    suspend fun createMenuCategory(category: MenuCategory): Result<MenuCategoryResponse> = withContext(Dispatchers.IO) {
        runCatching {
            api.createMenuCategory(category.toCreateDto())
        }
    }

    suspend fun toggleCategoryAvailability(id: Int, availability: ToggleAvailabilityRequest): Result<MenuCategoryResponse> = withContext(Dispatchers.IO) {
        runCatching {
            api.toggleCategoryAvailability(id, availability.toDto())
        }
    }

    suspend fun deleteCategory(id: Int): Result<MenuCategoryResponse> = withContext(Dispatchers.IO) {
        runCatching {
            api.deleteCategory(id)
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
        runCatching {
            val response = api.getMenuItems(categoryId, isAvailable, search, page, limit)
            response
        }.onFailure { error ->
            Log.e("MenuRepository", "API call failed with error: ${error.message}", error)
        }
    }

    suspend fun createMenuItem(menuItem: CreateMenuItemRequest): Result<MenuItemResponse> = withContext(Dispatchers.IO) {
        val dto = menuItem.toDto()
        runCatching {
            val response = api.createMenuItem(dto)
            response
        }
    }

    suspend fun updateMenuItem(id: Int, menuItem: UpdateMenuItemRequest): Result<MenuItemResponse> = withContext(Dispatchers.IO) {
        runCatching {
            api.updateMenuItem(id, menuItem.toDto())
        }
    }

    suspend fun toggleMenuItemAvailability(id: Int, availability: ToggleAvailabilityRequest): Result<MenuItemResponse> = withContext(Dispatchers.IO) {
        runCatching {
            api.toggleMenuItemAvailability(id, availability.toDto())
        }
    }

    suspend fun deleteMenuItem(id: Int): Result<MenuItemResponse> = withContext(Dispatchers.IO) {
        runCatching {
            api.deleteMenuItem(id)
        }
    }

}
