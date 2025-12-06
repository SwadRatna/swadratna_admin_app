package com.swadratna.swadratna_admin.data.model

import com.google.gson.annotations.SerializedName

data class AdminAnalyticsDto(
    @SerializedName("bars") val bars: List<AdminBarDto> = emptyList(),
    @SerializedName("cards") val cards: AdminCardsDto = AdminCardsDto(),
    @SerializedName("categories") val categories: List<AdminCategoryDto> = emptyList(),
    @SerializedName("referral_stats") val referralStats: AdminReferralStatsDto? = null
)

data class AdminReferralStatsDto(
    @SerializedName("active_referral_codes") val activeReferralCodes: Int,
    @SerializedName("top_referrers") val topReferrers: List<AdminTopReferrerDto>? = null,
    @SerializedName("total_referrals") val totalReferrals: Int,
    @SerializedName("total_referrers") val totalReferrers: Int
)

data class AdminTopReferrerDto(
    @SerializedName("amount_earned") val amountEarned: Double,
    @SerializedName("email") val email: String,
    @SerializedName("name") val name: String,
    @SerializedName("points_earned") val pointsEarned: Double,
    @SerializedName("referral_code") val referralCode: String,
    @SerializedName("total_referrals") val totalReferrals: Int,
    @SerializedName("user_id") val userId: Long
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
    val referralStatsDomain = referralStats?.let {
        ReferralStats(
            activeReferralCodes = it.activeReferralCodes,
            topReferrers = (it.topReferrers ?: emptyList()).map { ref ->
                TopReferrer(
                    amountEarned = ref.amountEarned,
                    email = ref.email,
                    name = ref.name,
                    pointsEarned = ref.pointsEarned,
                    referralCode = ref.referralCode,
                    totalReferrals = ref.totalReferrals,
                    userId = ref.userId
                )
            },
            totalReferrals = it.totalReferrals,
            totalReferrers = it.totalReferrers
        )
    }
    return Analytics(
        cards = cards,
        salesPerformance = emptyList(),
        monthlyOrderVolume = monthly,
        topProductCategories = topCategories,
        referralStats = referralStatsDomain
    )
}
