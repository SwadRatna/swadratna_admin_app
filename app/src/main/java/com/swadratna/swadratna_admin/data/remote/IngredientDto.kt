package com.swadratna.swadratna_admin.data.remote

import com.google.gson.annotations.SerializedName
import com.swadratna.swadratna_admin.data.model.Ingredient
import com.swadratna.swadratna_admin.data.model.CreateIngredientRequest
import com.swadratna.swadratna_admin.data.model.StockInRequest
import com.swadratna.swadratna_admin.data.model.StockOutRequest
import com.swadratna.swadratna_admin.data.model.WastageRequest
import com.swadratna.swadratna_admin.data.model.AdjustmentRequest

data class IngredientDto(
    val id: Int? = null,
    val name: String,
    val category: String,
    val unit: String,
    @SerializedName("reorder_level") val reorderLevel: Int,
    @SerializedName("cost_per_unit") val costPerUnit: Double,
    @SerializedName("location_id") val locationId: Int? = null,
    @SerializedName("current_stock") val currentStock: Int? = null,
    @SerializedName("is_active") val isActive: Boolean? = null,
    val status: String? = null
)

data class CreateIngredientDto(
    val name: String,
    val category: String,
    val unit: String,
    @SerializedName("reorder_level") val reorderLevel: Int,
    @SerializedName("cost_per_unit") val costPerUnit: Double,
    @SerializedName("location_id") val locationId: Int
)

data class UpdateIngredientDto(
    @SerializedName("reorder_level") val reorderLevel: Int,
    @SerializedName("cost_per_unit") val costPerUnit: Double
)

data class StockInRequestDto(
    @SerializedName("ingredient_id") val ingredientId: Int,
    val quantity: Int,
    @SerializedName("cost_per_unit") val costPerUnit: Double,
    @SerializedName("vendor_name") val vendorName: String? = null,
    @SerializedName("invoice_number") val invoiceNumber: String? = null,
    val notes: String? = null
)

data class StockOperationResponse(
    val success: Boolean = true,
    val message: String = ""
)

data class IngredientOperationResponse(
    val success: Boolean = true,
    val message: String = "",
    val ingredient: IngredientDto? = null
)

data class IngredientsResponse(
    @SerializedName("ingredients") val ingredients: List<IngredientDto>? = null,
    val total: Int = 0
)

fun IngredientDto.toDomain() = Ingredient(
    id = id,
    name = name,
    category = category,
    unit = unit,
    reorderLevel = reorderLevel,
    costPerUnit = costPerUnit,
    locationId = locationId,
    currentStock = currentStock
)

fun CreateIngredientRequest.toDto(locationId: Int) = CreateIngredientDto(
    name = name,
    category = category,
    unit = unit,
    reorderLevel = reorderLevel,
    costPerUnit = costPerUnit,
    locationId = locationId
)

fun StockInRequest.toDto() = StockInRequestDto(
    ingredientId = ingredientId,
    quantity = quantity,
    costPerUnit = costPerUnit,
    vendorName = vendorName,
    invoiceNumber = invoiceNumber,
    notes = notes
)

data class StockOutRequestDto(
    @SerializedName("ingredient_id") val ingredientId: Int,
    val quantity: Int,
    val reason: String? = null
)

fun StockOutRequest.toDto() = StockOutRequestDto(
    ingredientId = ingredientId,
    quantity = quantity,
    reason = reason
)

data class WastageRequestDto(
    @SerializedName("ingredient_id") val ingredientId: Int,
    val quantity: Int,
    val reason: String? = null
)

fun WastageRequest.toDto() = WastageRequestDto(
    ingredientId = ingredientId,
    quantity = quantity,
    reason = reason
)

data class AdjustmentRequestDto(
    @SerializedName("ingredient_id") val ingredientId: Int,
    @SerializedName("new_stock") val newStock: Int,
    val reason: String? = null
)

fun AdjustmentRequest.toDto() = AdjustmentRequestDto(
    ingredientId = ingredientId,
    newStock = newStock,
    reason = reason
)
