package com.swadratna.swadratna_admin.data.model

data class MenuCategory(
    val id: Int? = null,
    val name: String,
    val description: String,
    val displayOrder: Int,
    val isActive: Boolean
)

data class CreateMenuCategoryRequest(
    val name: String,
    val description: String,
    val display_order: Int,
    val is_active: Boolean
)