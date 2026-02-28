package com.swadratna.swadratna_admin.data.model

data class Ingredient(
    val id: Int? = null,
    val name: String,
    val category: String,
    val unit: String,
    val reorderLevel: Int,
    val costPerUnit: Double,
    val locationId: Int? = null,
    val currentStock: Int? = null
)

data class CreateIngredientRequest(
    val name: String,
    val category: String,
    val unit: String,
    val reorderLevel: Int,
    val costPerUnit: Double
)
