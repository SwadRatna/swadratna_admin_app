package com.swadratna.swadratna_admin.data.model

data class InventoryUsageItem(
    val ingredientId: Int,
    val ingredientName: String,
    val category: String,
    val unit: String,
    val usedQty: Int,
    val wastedQty: Int,
    val totalQty: Int,
    val costPerUnit: Double,
    val totalCost: Double,
    val transactions: Int
)

data class InventoryUsageTotals(
    val totalCost: Double,
    val totalItems: Int,
    val totalQuantity: Int,
    val totalTransactions: Int
)
