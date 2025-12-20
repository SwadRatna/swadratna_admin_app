package com.swadratna.swadratna_admin.data.model

data class InventoryMovement(
    val id: Int?,
    val ingredientId: Int?,
    val type: String?,
    val quantity: Int?,
    val costPerUnit: Double?,
    val totalCost: Double?,
    val createdAt: String?,
    val reason: String?,
    val vendorName: String?,
    val invoiceNumber: String?,
    val stockBefore: Int?,
    val stockAfter: Int?,
    val createdBy: Int?,
    val ingredientName: String?,
    val ingredientUnit: String?
)
