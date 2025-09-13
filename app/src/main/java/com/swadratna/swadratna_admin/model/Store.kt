package com.swadratna.swadratna_admin.model

import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class Store(
    val id: String,
    val name: String,
    val location: String,
    val address: String,
    val creationDate: LocalDate,
    val status: StoreStatus,
    val imageUrl: String? = null
) {
    fun getFormattedCreationDate(): String {
        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
        return creationDate.format(formatter)
    }
}

enum class StoreStatus {
    ACTIVE,
    INACTIVE,
    PENDING
}

data class StoreStats(
    val totalSales: Double,
    val totalOrders: Int,
    val averageOrderValue: Double,
    val topSellingItems: List<String>
)