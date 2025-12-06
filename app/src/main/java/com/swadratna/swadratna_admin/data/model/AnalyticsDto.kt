package com.swadratna.swadratna_admin.data.model

data class AnalyticsDto(
    val cards: CardsDto,
    val salesPerformance: List<SeriesDto>,
    val monthlyOrderVolume: List<MonthVolumeDto>,
    val topProductCategories: List<CategoryShareDto>
)
data class CardsDto(
    val totalSales: List<CardRowDto>,
    val totalRoi: List<CardRowDto>,
    val acquisition: List<CardRowDto>,
    val aov: List<CardRowDto>
)
data class CardRowDto(val title: String, val value: String, val deltaPct: Double)
data class SeriesDto(val name: String, val points: List<PointDto>) // Jan..Jun
data class PointDto(val xLabel: String, val y: Double)
data class MonthVolumeDto(val month: String, val dineIn: Double, val delivery: Double)
data class CategoryShareDto(val name: String, val percent: Double)

// Domain
data class Analytics(
    val cards: Cards,
    val salesPerformance: List<Series>,
    val monthlyOrderVolume: List<MonthVolume>,
    val topProductCategories: List<CategoryShare>,
    val referralStats: ReferralStats? = null
)
data class Cards(
    val totalSales: List<CardRow>,
    val totalRoi: List<CardRow>,
    val acquisition: List<CardRow>,
    val aov: List<CardRow>
)
data class CardRow(val title: String, val value: String, val deltaPct: Double)
data class Series(val name: String, val points: List<Point>)
data class Point(val xLabel: String, val y: Double)
data class MonthVolume(val month: String, val dineIn: Double, val delivery: Double)
data class CategoryShare(val name: String, val percent: Double)

data class ReferralStats(
    val activeReferralCodes: Int,
    val topReferrers: List<TopReferrer>,
    val totalReferrals: Int,
    val totalReferrers: Int
)

data class TopReferrer(
    val amountEarned: Double,
    val email: String,
    val name: String,
    val pointsEarned: Double,
    val referralCode: String,
    val totalReferrals: Int,
    val userId: Long
)
