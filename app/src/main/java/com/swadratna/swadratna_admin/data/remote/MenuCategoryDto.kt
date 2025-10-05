package com.swadratna.swadratna_admin.data.remote

import com.google.gson.annotations.SerializedName
import com.swadratna.swadratna_admin.data.model.MenuCategory

data class MenuCategoryDto(
    val id: Int? = null,
    val name: String,
    val description: String,
    @SerializedName("display_order")
    val displayOrder: Int,
    @SerializedName("is_active")
    val isActive: Boolean
)

data class CreateMenuCategoryDto(
    val name: String,
    val description: String,
    @SerializedName("display_order")
    val displayOrder: Int,
    @SerializedName("is_active")
    val isActive: Boolean
)

data class MenuCategoryResponse(
    val success: Boolean,
    val message: String,
    val data: MenuCategoryDto? = null
)

fun MenuCategoryDto.toDomain() = MenuCategory(
    id = id,
    name = name,
    description = description,
    displayOrder = displayOrder,
    isActive = isActive
)

fun MenuCategory.toCreateDto() = CreateMenuCategoryDto(
    name = name,
    description = description,
    displayOrder = displayOrder,
    isActive = isActive
)