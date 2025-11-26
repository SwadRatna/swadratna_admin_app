package com.swadratna.swadratna_admin.data.model

import com.google.gson.annotations.SerializedName

data class AdminAnalyticsDto(
    @SerializedName("bars") val bars: List<AdminBarDto> = emptyList(),
    @SerializedName("cards") val cards: AdminCardsDto = AdminCardsDto(),
    @SerializedName("categories") val categories: List<AdminCategoryDto> = emptyList()
)

data class AdminBarDto(
    @SerializedName("month") val month: String,
    @SerializedName("dineIn") val dineIn: Double,
    @SerializedName("delivery") val delivery: Double,
    @SerializedName("takeaway") val takeaway: Double
)

data class AdminCardsDto(
    @SerializedName("totalSales") val totalSales: List<AdminCardRowDto> = emptyList(),
    @SerializedName("customer_acquisition") val customerAcquisition: List<AdminAcquisitionRowDto> = emptyList()
)

data class AdminCardRowDto(
    @SerializedName("title") val title: String,
    @SerializedName("value") val value: String,
    @SerializedName("change") val change: Double
)

data class AdminAcquisitionRowDto(
    @SerializedName("title") val title: String,
    @SerializedName("number") val number: String,
    @SerializedName("change") val change: Double
)

data class AdminCategoryDto(
    @SerializedName("name") val name: String,
    @SerializedName("value") val value: Double
)

fun AdminAnalyticsDto.toDomain(): Analytics {
    val cards = Cards(
        totalSales = cards.totalSales.map { CardRow(it.title, it.value, it.change) },
        totalRoi = emptyList(),
        acquisition = cards.customerAcquisition.map { CardRow(it.title, it.number, it.change) },
        aov = emptyList()
    )
    val monthly = bars.map { MonthVolume(it.month, it.dineIn, it.delivery) }
    val topCategories = categories.map { CategoryShare(it.name, it.value) }
    return Analytics(
        cards = cards,
        salesPerformance = emptyList(),
        monthlyOrderVolume = monthly,
        topProductCategories = topCategories
    )
}
