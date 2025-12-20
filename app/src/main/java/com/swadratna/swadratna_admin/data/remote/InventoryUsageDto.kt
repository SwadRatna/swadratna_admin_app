package com.swadratna.swadratna_admin.data.remote

import com.google.gson.annotations.SerializedName
import com.swadratna.swadratna_admin.data.model.InventoryUsageItem
import com.swadratna.swadratna_admin.data.model.InventoryUsageTotals

data class InventoryUsageItemDto(
    @SerializedName("ingredient_id") val ingredientId: Int,
    @SerializedName("ingredient_name") val ingredientName: String,
    val category: String,
    val unit: String,
    @SerializedName("used_qty") val usedQty: Int,
    @SerializedName("wasted_qty") val wastedQty: Int,
    @SerializedName("total_qty") val totalQty: Int,
    @SerializedName("cost_per_unit") val costPerUnit: Double,
    @SerializedName("total_cost") val totalCost: Double,
    val transactions: Int
)

data class InventoryUsageTotalsDto(
    @SerializedName("total_cost") val totalCost: Double,
    @SerializedName("total_items") val totalItems: Int,
    @SerializedName("total_quantity") val totalQuantity: Int,
    @SerializedName("total_transactions") val totalTransactions: Int
)

data class InventoryUsageResponse(
    @SerializedName("period") val period: String,
    @SerializedName("type") val type: String,
    @SerializedName("start_date") val startDate: String?,
    @SerializedName("end_date") val endDate: String?,
    @SerializedName("items") val items: List<InventoryUsageItemDto>?,
    @SerializedName("totals") val totals: InventoryUsageTotalsDto?
)

fun InventoryUsageItemDto.toDomain() = InventoryUsageItem(
    ingredientId = ingredientId,
    ingredientName = ingredientName,
    category = category,
    unit = unit,
    usedQty = usedQty,
    wastedQty = wastedQty,
    totalQty = totalQty,
    costPerUnit = costPerUnit,
    totalCost = totalCost,
    transactions = transactions
)

fun InventoryUsageTotalsDto.toDomain() = InventoryUsageTotals(
    totalCost = totalCost,
    totalItems = totalItems,
    totalQuantity = totalQuantity,
    totalTransactions = totalTransactions
)
