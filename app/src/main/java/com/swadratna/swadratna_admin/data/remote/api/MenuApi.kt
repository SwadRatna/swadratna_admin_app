package com.swadratna.swadratna_admin.data.remote.api

import com.swadratna.swadratna_admin.data.remote.CreateMenuCategoryDto
import com.swadratna.swadratna_admin.data.remote.MenuCategoryDto
import com.swadratna.swadratna_admin.data.remote.MenuCategoryResponse
import com.swadratna.swadratna_admin.data.remote.MenuItemDto
import com.swadratna.swadratna_admin.data.remote.CreateMenuItemDto
import com.swadratna.swadratna_admin.data.remote.UpdateMenuItemDto
import com.swadratna.swadratna_admin.data.remote.ToggleAvailabilityDto
import com.swadratna.swadratna_admin.data.remote.MenuItemResponse
import com.swadratna.swadratna_admin.data.remote.MenuItemsListResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.PATCH
import retrofit2.http.DELETE
import retrofit2.http.Path
import retrofit2.http.Query

interface MenuApi {
    @GET("api/v1/menu")
    suspend fun getMenu(
        @Query("category") category: String? = null
    ): List<MenuItemDto>
    
    @GET("api/v1/admin/menu/categories")
    suspend fun getMenuCategories(
    ): List<MenuCategoryDto>
    
    @POST("api/v1/admin/menu/categories")
    suspend fun createMenuCategory(
        @Body category: CreateMenuCategoryDto
    ): MenuCategoryResponse
    
    @PATCH("api/v1/admin/menu/categories/{id}/toggle")
    suspend fun toggleCategoryAvailability(
        @Path("id") id: Int,
        @Body availability: ToggleAvailabilityDto
    ): MenuCategoryResponse
    
    @DELETE("api/v1/admin/menu/categories/{id}")
    suspend fun deleteCategory(
        @Path("id") id: Int
    ): MenuCategoryResponse
    
    // Menu Items endpoints
    @GET("api/v1/admin/menu/items")
    suspend fun getMenuItems(
        @Query("category_id") categoryId: Int? = null,
        @Query("is_available") isAvailable: Boolean? = null,
        @Query("search") search: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): MenuItemsListResponse
    
    @POST("api/v1/admin/menu/items")
    suspend fun createMenuItem(
        @Body menuItem: CreateMenuItemDto
    ): MenuItemResponse
    
    @PUT("api/v1/admin/menu/items/{id}")
    suspend fun updateMenuItem(
        @Path("id") id: Int,
        @Body menuItem: UpdateMenuItemDto
    ): MenuItemResponse
    
    @PATCH("api/v1/admin/menu/items/{id}/availability")
    suspend fun toggleMenuItemAvailability(
        @Path("id") id: Int,
        @Body availability: ToggleAvailabilityDto
    ): MenuItemResponse
    
    @DELETE("api/v1/admin/menu/items/{id}")
    suspend fun deleteMenuItem(
        @Path("id") id: Int
    ): MenuItemResponse
}
