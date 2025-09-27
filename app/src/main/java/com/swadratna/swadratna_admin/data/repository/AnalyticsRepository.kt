package com.swadratna.swadratna_admin.data.repository

import com.swadratna.swadratna_admin.data.model.Analytics
import com.swadratna.swadratna_admin.data.model.AnalyticsDto
import com.swadratna.swadratna_admin.data.model.CardRow
import com.swadratna.swadratna_admin.data.model.Cards
import com.swadratna.swadratna_admin.data.model.CategoryShare
import com.swadratna.swadratna_admin.data.model.MonthVolume
import com.swadratna.swadratna_admin.data.model.Point
import com.swadratna.swadratna_admin.data.model.Series
import com.swadratna.swadratna_admin.data.remote.api.AnalyticsApi
import javax.inject.Inject
import javax.inject.Singleton

interface AnalyticsRepository {
    suspend fun loadDashboard(
        franchise: String?,
        from: String?,
        to: String?
    ): Analytics
}

@Singleton
class AnalyticsRepositoryImpl @Inject constructor(
    private val api: AnalyticsApi
) : AnalyticsRepository {

    override suspend fun loadDashboard(franchise: String?, from: String?, to: String?): Analytics {
        val want = false
        return if (want) {
            api.getDashboard(franchise, from, to).toDomain()
        } else {
            mockAnalytics()
        }
    }

    private fun AnalyticsDto.toDomain(): Analytics = Analytics(
        cards = Cards(
            totalSales = cards.totalSales.map { CardRow(it.title, it.value, it.deltaPct) },
            totalRoi = cards.totalRoi.map { CardRow(it.title, it.value, it.deltaPct) },
            acquisition = cards.acquisition.map { CardRow(it.title, it.value, it.deltaPct) },
            aov = cards.aov.map { CardRow(it.title, it.value, it.deltaPct) }
        ),
        salesPerformance = salesPerformance.map { s ->
            Series(s.name, s.points.map { Point(it.xLabel, it.y) })
        },
        monthlyOrderVolume = monthlyOrderVolume.map { MonthVolume(it.month, it.dineIn, it.delivery) },
        topProductCategories = topProductCategories.map { CategoryShare(it.name, it.percent) }
    )

    private fun mockAnalytics(): Analytics {
        val months = listOf("Jan","Feb","Mar","Apr","May","Jun")
        val series = listOf(
            Series("Burger Barn HQ", listOf(3.0,1.0,2.5,2.8,3.1,4.0).mapIndexed { i,v -> Point(months[i], v*1000) }),
            Series("Pizza Palace North", listOf(2.5,2.2,2.6,2.4,2.6,2.3).mapIndexed { i,v -> Point(months[i], v*1000) }),
            Series("Taco Town South", listOf(1.0,2.8,2.1,2.4,2.6,2.1).mapIndexed { i,v -> Point(months[i], v*1000) })
        )
        val bars = listOf(
            MonthVolume("Jan", 2.4e3, 1.6e3),
            MonthVolume("Feb", 2.1e3, 1.7e3),
            MonthVolume("Mar", 2.8e3, 1.8e3),
            MonthVolume("Apr", 2.3e3, 1.9e3),
            MonthVolume("May", 2.7e3, 2.1e3)
        )
        val categories = listOf(
            CategoryShare("Pepperoni Pizza", 38.0),
            CategoryShare("Spicy Taco", 24.0),
            CategoryShare("Teriyaki Noodles", 18.0),
            CategoryShare("Veggie Burger", 12.0),
            CategoryShare("Other", 8.0)
        )
        val cards = Cards(
            totalSales = listOf(
                CardRow("Burger Barn HQ", "$120k", +12.0),
                CardRow("Pizza Palace North", "$95k", +8.0),
                CardRow("Taco Town South", "$72k", -5.0)
            ),
            totalRoi = listOf(
                CardRow("Burger Barn HQ", "18%", +2.0),
                CardRow("Pizza Palace North", "15%", +1.0),
                CardRow("Taco Town South", "10%", -1.0)
            ),
            acquisition = listOf(
                CardRow("Burger Barn HQ", "1.2k", +15.0),
                CardRow("Pizza Palace North", "980", +10.0),
                CardRow("Taco Town South", "650", -2.0)
            ),
            aov = listOf(
                CardRow("Burger Barn HQ", "$25.50", +1.0),
                CardRow("Pizza Palace North", "$32.10", +0.5),
                CardRow("Taco Town South", "$18.90", -0.3)
            )
        )
        return Analytics(cards, series, bars, categories)
    }
}
