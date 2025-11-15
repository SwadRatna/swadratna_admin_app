package com.swadratna.swadratna_admin.data.remote

import com.google.gson.annotations.SerializedName
import com.swadratna.swadratna_admin.data.model.MenuItem
import com.swadratna.swadratna_admin.data.model.CreateMenuItemRequest
import com.swadratna.swadratna_admin.data.model.UpdateMenuItemRequest
import com.swadratna.swadratna_admin.data.model.ToggleAvailabilityRequest
import com.swadratna.swadratna_admin.data.model.NutritionalInfo

data class MenuItemDto(
    val id: Int? = null,
    @SerializedName("tenant_id")
    val tenantId: Int? = null,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null,
    @SerializedName("category_id")
    val categoryId: Int,
    val name: String,
    val description: String,
    val price: Double,
    val currency: String = "INR",
    @SerializedName("discount_percentage")
    val discountPercentage: Double? = null,
    @SerializedName("discounted_price")
    val discountedPrice: Double? = null,
    @SerializedName("display_order")
    val displayOrder: Int,
    val image: String? = null,
    @SerializedName("is_available")
    val isAvailable: Boolean = true,
    @SerializedName("allergen_info")
    val allergenInfo: List<String>? = null,
    @SerializedName("nutritional_info")
    val nutritionalInfo: NutritionalInfo? = null,
    @SerializedName("preparation_time")
    val preparationTime: Int? = null,
    @SerializedName("spice_level")
    val spiceLevel: String? = null,
    val tags: List<String>? = null,
    @SerializedName("category_name")
    val categoryName: String? = null,
    val ingredients: List<String>? = null,
    @SerializedName("is_vegetarian")
    val isVegetarian: Boolean? = null,
    @SerializedName("spicy_level")
    val spicyLevel: Int? = null,
    @SerializedName("unavailable_reason")
    val unavailableReason: String? = null
)

data class CreateMenuItemDto(
    @SerializedName("category_id")
    val categoryId: Int,
    val name: String,
    val description: String,
    val price: Double,
    val currency: String = "INR",
    @SerializedName("discount_percentage")
    val discountPercentage: Double = 0.0,
    @SerializedName("discounted_price")
    val discountedPrice: Double = 0.0,
    @SerializedName("display_order")
    val displayOrder: Int,
    val image: String? = null,
    @SerializedName("is_available")
    val isAvailable: Boolean = true,
    @SerializedName("allergen_info")
    val allergenInfo: List<String> = emptyList(),
    @SerializedName("nutritional_info")
    val nutritionalInfo: NutritionalInfo? = null,
    @SerializedName("preparation_time")
    val preparationTime: Int? = null,
    @SerializedName("spice_level")
    val spiceLevel: String? = null,
    @SerializedName("spicy_level")
    val spicyLevel: Int = 0,
    val tags: List<String> = emptyList(),
    val id: Int = 0,
    @SerializedName("tenant_id")
    val tenantId: Int = 0,
    @SerializedName("created_at")
    val createdAt: String = "",
    @SerializedName("updated_at")
    val updatedAt: String = "",
    val ingredients: List<String> = emptyList(),
    @SerializedName("is_vegetarian")
    val isVegetarian: Boolean = true,
    @SerializedName("unavailable_reason")
    val unavailableReason: String = ""
)

data class UpdateMenuItemDto(
    val id: Int,
    @SerializedName("tenant_id")
    val tenantId: Int,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("category_id")
    val categoryId: Int,
    val name: String,
    val description: String,
    val price: Double,
    val currency: String,
    @SerializedName("discount_percentage")
    val discountPercentage: Double?,
    @SerializedName("discounted_price")
    val discountedPrice: Double?,
    @SerializedName("display_order")
    val displayOrder: Int,
    val image: String?,
    @SerializedName("is_available")
    val isAvailable: Boolean,
    @SerializedName("allergen_info")
    val allergenInfo: List<String>,
    @SerializedName("nutritional_info")
    val nutritionalInfo: NutritionalInfo?,
    @SerializedName("preparation_time")
    val preparationTime: Int?,
    @SerializedName("spice_level")
    val spiceLevel: String?,
    val tags: List<String>,
    val ingredients: List<String>,
    @SerializedName("is_vegetarian")
    val isVegetarian: Boolean,
    @SerializedName("spicy_level")
    val spicyLevel: Int,
    @SerializedName("unavailable_reason")
    val unavailableReason: String
)

data class ToggleAvailabilityDto(
    @SerializedName("is_available")
    val isAvailable: Boolean
)

data class MenuItemResponse(
    val success: Boolean = true, // Default to true for successful HTTP responses
    val message: String,
    val data: MenuItemDto? = null
)

data class MenuItemsListResponse(
    val success: Boolean = true,
    val message: String = "",
    val items: List<MenuItemDto>? = null,
    val pagination: PaginationInfo? = null
)

data class PaginationInfo(
    val total: Int = 0,
    val page: Int = 1,
    val limit: Int = 20,
    @SerializedName("total_pages")
    val totalPages: Int = 1,
    @SerializedName("has_next")
    val hasNext: Boolean = false,
    @SerializedName("has_prev")
    val hasPrev: Boolean = false
)

fun MenuItemDto.toDomain() = MenuItem(
    id = id,
    tenantId = tenantId,
    createdAt = createdAt,
    updatedAt = updatedAt,
    categoryId = categoryId,
    name = name,
    description = description,
    price = price,
    currency = currency,
    discountPercentage = discountPercentage,
    discountedPrice = discountedPrice,
    displayOrder = displayOrder,
    image = image,
    isAvailable = isAvailable,
    allergenInfo = allergenInfo ?: emptyList(),
    nutritionalInfo = nutritionalInfo,
    preparationTime = preparationTime,
    spiceLevel = spiceLevel,
    tags = tags ?: emptyList(),
    categoryName = categoryName,
    ingredients = ingredients ?: emptyList(),
    isVegetarian = isVegetarian,
    spicyLevel = spicyLevel,
    unavailableReason = unavailableReason
)

fun CreateMenuItemRequest.toDto() = CreateMenuItemDto(
    categoryId = categoryId,
    name = name,
    description = description,
    price = price,
    currency = currency,
    discountPercentage = discountPercentage,
    discountedPrice = discountedPrice,
    displayOrder = displayOrder,
    image = image,
    isAvailable = isAvailable,
    allergenInfo = allergenInfo,
    nutritionalInfo = nutritionalInfo,
    preparationTime = preparationTime,
    spiceLevel = spiceLevel,
    spicyLevel = spicyLevel,
    tags = tags,
    id = id,
    tenantId = tenantId,
    createdAt = createdAt,
    updatedAt = updatedAt,
    ingredients = ingredients,
    isVegetarian = isVegetarian,
    unavailableReason = unavailableReason
)

fun UpdateMenuItemRequest.toDto() = UpdateMenuItemDto(
    id = id,
    tenantId = tenantId,
    createdAt = createdAt,
    updatedAt = updatedAt,
    categoryId = categoryId,
    name = name,
    description = description,
    price = price,
    currency = currency,
    discountPercentage = discountPercentage,
    discountedPrice = discountedPrice,
    displayOrder = displayOrder,
    image = image,
    isAvailable = isAvailable,
    allergenInfo = allergenInfo,
    nutritionalInfo = nutritionalInfo,
    preparationTime = preparationTime,
    spiceLevel = spiceLevel,
    tags = tags,
    ingredients = ingredients,
    isVegetarian = isVegetarian,
    spicyLevel = spicyLevel,
    unavailableReason = unavailableReason
)

fun ToggleAvailabilityRequest.toDto() = ToggleAvailabilityDto(
    isAvailable = isAvailable
)

fun MenuItem.toDto() = MenuItemDto(
    id = id,
    tenantId = tenantId,
    createdAt = createdAt,
    updatedAt = updatedAt,
    categoryId = categoryId,
    name = name,
    description = description,
    price = price,
    currency = currency,
    discountPercentage = discountPercentage,
    discountedPrice = discountedPrice,
    displayOrder = displayOrder,
    image = image,
    isAvailable = isAvailable,
    allergenInfo = allergenInfo,
    nutritionalInfo = nutritionalInfo,
    preparationTime = preparationTime,
    spiceLevel = spiceLevel,
    tags = tags,
    categoryName = categoryName,
    ingredients = ingredients,
    isVegetarian = isVegetarian,
    spicyLevel = spicyLevel,
    unavailableReason = unavailableReason
)
