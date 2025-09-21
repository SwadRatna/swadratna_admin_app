package com.swadratna.swadratna_admin.data.repository

import com.swadratna.swadratna_admin.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

interface AnalyticsRepository {
    fun getAnalyticsData(): Flow<Result<AnalyticsData>>
}

@Singleton
class AnalyticsRepositoryImpl @Inject constructor() : AnalyticsRepository {
    
    override fun getAnalyticsData(): Flow<Result<AnalyticsData>> = flow {
        try {
            // Simulate API call
            // If API call is successful, return the data
            // Otherwise, return mock data
            emit(Result.success(getMockAnalyticsData()))
        } catch (e: Exception) {
            emit(Result.success(getMockAnalyticsData()))
        }
    }
    
    private fun getMockAnalyticsData(): AnalyticsData {
        val franchises = listOf(
            Franchise("1", "Burger Barn"),
            Franchise("2", "Pizza Palace"),
            Franchise("3", "Taco Town")
        )
        
        val totalSales = TotalSales(
            totalValue = "$120k",
            percentChange = 12f,
            franchiseData = listOf(
                FranchiseSalesData("1", "Burger Barn", "$120k", 12f),
                FranchiseSalesData("2", "Pizza Palace", "$95k", 8f),
                FranchiseSalesData("3", "Taco Town", "$72k", -5f)
            )
        )
        
        val totalROI = TotalROI(
            totalValue = "15%",
            percentChange = 2f,
            franchiseData = listOf(
                FranchiseROIData("1", "Burger Barn", "15%", 2f),
                FranchiseROIData("2", "Pizza Palace", "13%", 1f),
                FranchiseROIData("3", "Taco Town", "10%", -3f)
            )
        )
        
        val customerAcquisition = CustomerAcquisition(
            totalValue = "1.2k",
            percentChange = 15f,
            franchiseData = listOf(
                FranchiseCustomerData("1", "Burger Barn", "1.2k", 15f),
                FranchiseCustomerData("2", "Pizza Palace", "680", 185f),
                FranchiseCustomerData("3", "Taco Town", "650", -8f)
            )
        )
        
        val averageOrderValue = AverageOrderValue(
            totalValue = "$25.50",
            percentChange = 5f,
            franchiseData = listOf(
                FranchiseAverageOrderData("1", "Burger Barn", "$25.50", 5f),
                FranchiseAverageOrderData("2", "Pizza Palace", "$32.10", -6.5f),
                FranchiseAverageOrderData("3", "Taco Town", "$18.90", -8.3f)
            )
        )
        
        val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul")
        
        val salesPerformance = SalesPerformance(
            timeFrame = "1 month",
            franchisePerformance = listOf(
                FranchisePerformanceData(
                    "1", "Burger Barn",
                    months.mapIndexed { index, month ->
                        PerformancePoint(month, listOf(1.5f, 1.8f, 2.0f, 1.7f, 1.9f, 2.2f, 2.1f)[index])
                    }
                ),
                FranchisePerformanceData(
                    "2", "Pizza Palace",
                    months.mapIndexed { index, month ->
                        PerformancePoint(month, listOf(2.0f, 1.5f, 3.0f, 2.5f, 2.2f, 1.8f, 2.3f)[index])
                    }
                ),
                FranchisePerformanceData(
                    "3", "Taco Town",
                    months.mapIndexed { index, month ->
                        PerformancePoint(month, listOf(1.2f, 1.4f, 1.3f, 1.5f, 1.7f, 1.6f, 1.8f)[index])
                    }
                )
            )
        )
        
        val monthlyOrderVolume = MonthlyOrderVolume(
            timeFrame = "1 month",
            volumeData = months.mapIndexed { index, month ->
                MonthlyVolumeData(
                    month,
                    listOf(1.2f, 1.5f, 1.3f, 1.7f, 1.4f, 1.6f, 1.8f)[index],
                    listOf(0.8f, 1.0f, 0.9f, 1.2f, 1.1f, 1.3f, 1.4f)[index]
                )
            }
        )
        
        val topSellingProductCategories = TopSellingProductCategories(
            timeFrame = "1 month",
            categories = listOf(
                ProductCategory("Pepperoni Pizza", 45f, "#4285F4"),
                ProductCategory("Spicy Taco", 30f, "#34A853"),
                ProductCategory("Teriyaki Noodles", 25f, "#FBBC05")
            )
        )
        
        return AnalyticsData(
            franchises = franchises,
            totalSales = totalSales,
            totalROI = totalROI,
            customerAcquisition = customerAcquisition,
            averageOrderValue = averageOrderValue,
            salesPerformance = salesPerformance,
            monthlyOrderVolume = monthlyOrderVolume,
            topSellingProductCategories = topSellingProductCategories
        )
    }
}