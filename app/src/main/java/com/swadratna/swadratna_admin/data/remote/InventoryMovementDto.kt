package com.swadratna.swadratna_admin.data.remote

import com.google.gson.annotations.SerializedName
import com.swadratna.swadratna_admin.data.model.InventoryMovement

data class InventoryMovementDto(
    val id: Int? = null,
    @SerializedName("ingredient_id") val ingredientId: Int? = null,
    @SerializedName("movement_type") val type: String? = null,
    val quantity: Int? = null,
    @SerializedName("cost_per_unit") val costPerUnit: Double? = null,
    @SerializedName("total_cost") val totalCost: Double? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    val reason: String? = null,
    @SerializedName("vendor_name") val vendorName: String? = null,
    @SerializedName("invoice_number") val invoiceNumber: String? = null,
    @SerializedName("stock_before") val stockBefore: Int? = null,
    @SerializedName("stock_after") val stockAfter: Int? = null,
    @SerializedName("created_by") val createdBy: Int? = null,
    @SerializedName("ingredient_name") val ingredientName: String? = null,
    @SerializedName("ingredient_unit") val ingredientUnit: String? = null
)

data class InventoryMovementsResponse(
    @SerializedName("movements") val movements: List<InventoryMovementDto>? = null,
    val total: Int = 0,
    val page: Int? = null,
    val limit: Int? = null
)

fun InventoryMovementDto.toDomain() = InventoryMovement(
    id = id,
    ingredientId = ingredientId,
    type = type,
    quantity = quantity,
    costPerUnit = costPerUnit,
    totalCost = totalCost,
    createdAt = createdAt,
    reason = reason,
    vendorName = vendorName,
    invoiceNumber = invoiceNumber,
    stockBefore = stockBefore,
    stockAfter = stockAfter,
    createdBy = createdBy,
    ingredientName = ingredientName,
    ingredientUnit = ingredientUnit
)
