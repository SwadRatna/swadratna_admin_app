package com.swadratna.swadratna_admin.model

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
    val targetFranchises: String = "All",
    val menuCategories: List<String> = emptyList()
) {
    fun getFormattedDateRange(): String {
        val formatter = DateTimeFormatter.ofPattern("dd MMM")
        return "${startDate.format(formatter)} - ${endDate.format(formatter)}"
    }
    
    fun getDurationInDays(): Int {
        return endDate.toEpochDay().toInt() - startDate.toEpochDay().toInt() + 1
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