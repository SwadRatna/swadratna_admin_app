package com.swadratna.swadratna_admin.data.model

data class AnalyticsData(
    val franchises: List<Franchise>,
    val totalSales: TotalSales,
    val totalROI: TotalROI,
    val customerAcquisition: CustomerAcquisition,
    val averageOrderValue: AverageOrderValue,
    val salesPerformance: SalesPerformance,
    val monthlyOrderVolume: MonthlyOrderVolume,
    val topSellingProductCategories: TopSellingProductCategories
)

data class Franchise(
    val id: String,
    val name: String
)

data class TotalSales(
    val totalValue: String,
    val percentChange: Float,
    val franchiseData: List<FranchiseSalesData>
)

data class FranchiseSalesData(
    val franchiseId: String,
    val franchiseName: String,
    val value: String,
    val percentChange: Float
)

data class TotalROI(
    val totalValue: String,
    val percentChange: Float,
    val franchiseData: List<FranchiseROIData>
)

data class FranchiseROIData(
    val franchiseId: String,
    val franchiseName: String,
    val value: String,
    val percentChange: Float
)

data class CustomerAcquisition(
    val totalValue: String,
    val percentChange: Float,
    val franchiseData: List<FranchiseCustomerData>
)

data class FranchiseCustomerData(
    val franchiseId: String,
    val franchiseName: String,
    val value: String,
    val percentChange: Float
)

data class AverageOrderValue(
    val totalValue: String,
    val percentChange: Float,
    val franchiseData: List<FranchiseAverageOrderData>
)

data class FranchiseAverageOrderData(
    val franchiseId: String,
    val franchiseName: String,
    val value: String,
    val percentChange: Float
)

data class SalesPerformance(
    val timeFrame: String,
    val franchisePerformance: List<FranchisePerformanceData>
)

data class FranchisePerformanceData(
    val franchiseId: String,
    val franchiseName: String,
    val performanceData: List<PerformancePoint>
)

data class PerformancePoint(
    val month: String,
    val value: Float
)

data class MonthlyOrderVolume(
    val timeFrame: String,
    val volumeData: List<MonthlyVolumeData>
)

data class MonthlyVolumeData(
    val month: String,
    val dineIn: Float,
    val takeaway: Float
)

data class TopSellingProductCategories(
    val timeFrame: String,
    val categories: List<ProductCategory>
)

data class ProductCategory(
    val name: String,
    val percentage: Float,
    val color: String
)