package com.swadratna.swadratna_admin.data.remote.api

import com.swadratna.swadratna_admin.data.remote.MenuItemDto
import retrofit2.http.GET
import retrofit2.http.Query

interface MenuApi {
    @GET("menu")
    suspend fun getMenu(
        @Query("category") category: String? = null
    ): List<MenuItemDto>
}
