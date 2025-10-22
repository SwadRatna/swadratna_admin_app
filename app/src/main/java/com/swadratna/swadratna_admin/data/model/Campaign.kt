package com.swadratna.swadratna_admin.data.model

import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class Campaign(
    val id: String,
    val title: String,
    val description: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val status: CampaignStatus,
    val type: CampaignType,
    val discount: Int,
    val storeCount: Int,
    val imageUrl: String? = null,
    val targetStores: String = "All",
    val menuCategories: List<String> = emptyList(),
    val targetFranchiseIds: List<Int> = emptyList(),
    val targetCategoryIds: List<Int> = emptyList()
) {
    fun getFormattedDateRange(): String {
        val formatter = DateTimeFormatter.ofPattern("dd MMM")
        return "${startDate.format(formatter)} - ${endDate.format(formatter)}"
    }
}

enum class CampaignStatus {
    ACTIVE,
    SCHEDULED,
    COMPLETED,
    DRAFT
}

enum class CampaignType {
    DISCOUNT,
    BOGO,
    SPECIAL_OFFER,
    SEASONAL,
    FLASH_SALE
}

data class CampaignStats(
    val totalViews: Int,
    val totalClicks: Int,
    val conversionRate: Float,
    val revenue: Double
)