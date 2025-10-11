package com.swadratna.swadratna_admin.data.model

data class NutritionalInfo(
    val calories: Any? = null,  // Can be Int or String
    val protein: String? = null,
    val carbohydrates: String? = null,
    val fat: String? = null,
    val fiber: String? = null,
    val sugar: String? = null,
    val sodium: String? = null,
    val cholesterol: String? = null,
    val msg: String? = null,  // MSG content
    val vitamins: Map<String, String>? = null,
    val minerals: Map<String, String>? = null
)

data class MenuItem(
    val id: Int? = null,
    val tenantId: Int? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val categoryId: Int,
    val name: String,
    val description: String,
    val price: Double,
    val currency: String = "INR",
    val discountPercentage: Double? = null,
    val discountedPrice: Double? = null,
    val displayOrder: Int,
    val image: String? = null,
    val isAvailable: Boolean = true,
    val allergenInfo: List<String> = emptyList(),
    val nutritionalInfo: NutritionalInfo? = null,
    val preparationTime: Int? = null,
    val spiceLevel: String? = null,
    val tags: List<String> = emptyList(),
    val categoryName: String? = null,
    val ingredients: List<String> = emptyList(),
    val isVegetarian: Boolean? = null,
    val spicyLevel: Int? = null,
    val unavailableReason: String? = null
)

data class CreateMenuItemRequest(
    val categoryId: Int,
    val name: String,
    val description: String,
    val price: Double,
    val currency: String = "INR",
    val discountPercentage: Double? = null,
    val discountedPrice: Double? = null,
    val displayOrder: Int,
    val image: String? = null,
    val isAvailable: Boolean = true,
    val allergenInfo: List<String> = emptyList(),
    val nutritionalInfo: NutritionalInfo? = null,
    val preparationTime: Int? = null,
    val spiceLevel: String? = null,
    val tags: List<String> = emptyList()
)

data class UpdateMenuItemRequest(
    val id: Int,
    val tenantId: Int,
    val createdAt: String,
    val updatedAt: String,
    val categoryId: Int,
    val name: String,
    val description: String,
    val price: Double,
    val currency: String,
    val discountPercentage: Double?,
    val discountedPrice: Double?,
    val displayOrder: Int,
    val image: String?,
    val isAvailable: Boolean,
    val allergenInfo: List<String>,
    val nutritionalInfo: NutritionalInfo?,
    val preparationTime: Int?,
    val spiceLevel: String?,
    val tags: List<String>
)

data class ToggleAvailabilityRequest(
    val isAvailable: Boolean
)
