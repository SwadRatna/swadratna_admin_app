package com.swadratna.swadratna_admin.data.repository

import com.swadratna.swadratna_admin.data.model.ActivityItemResponse
import com.swadratna.swadratna_admin.data.model.DashboardResponse
import com.swadratna.swadratna_admin.data.model.StoreItemResponse
import com.swadratna.swadratna_admin.data.remote.api.DashboardApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class DashboardRepository @Inject constructor(
    private val api: DashboardApi
) {
    suspend fun getDashboardData(): DashboardResponse {
        return try {
            withContext(Dispatchers.IO) {
                api.getDashboardData()
            }
        } catch (e: IOException) {
            getMockDashboardData()
        } catch (e: HttpException) {
            getMockDashboardData()
        }
    }

    private fun getMockDashboardData(): DashboardResponse {
        return DashboardResponse(
            totalCampaigns = 0,
            campaignsChange = "+0% since last month",
            activeStore = 0,
            storeChange = "+0% last 3 months",
            topSeller = "NA",
            topSellerMetric = "00% of total sales",
            newUsers = 0,
            newUsersChange = "0% compared to avg.",
            recentActivities = listOf(
                ActivityItemResponse("New campaign \"Summer Blast\" launched", "2 hours ago"),
                ActivityItemResponse("Store \"Downtown Deli\" updated menu", "1 day ago"),
                ActivityItemResponse("Report for Q3 2023 generated", "3 days ago")
            ),
            topStore = listOf(
                StoreItemResponse("Coastal Grill", "$125,000"),
                StoreItemResponse("Urban Eatery", "$110,500"),
                StoreItemResponse("Parkside Bistro", "$98,200")
            )
        )
    }
}
