package com.swadratna.swadratna_admin.data.model

data class StockInRequest(
    val ingredientId: Int,
    val quantity: Int,
    val costPerUnit: Double,
    val vendorName: String? = null,
    val invoiceNumber: String? = null,
    val notes: String? = null
)

data class StockOutRequest(
    val ingredientId: Int,
    val quantity: Int,
    val reason: String? = null
)

data class WastageRequest(
    val ingredientId: Int,
    val quantity: Int,
    val reason: String? = null
)

data class AdjustmentRequest(
    val ingredientId: Int,
    val newStock: Int,
    val reason: String? = null
)
