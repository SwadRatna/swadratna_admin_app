package com.swadratna.swadratna_admin.data.model

import com.swadratna.swadratna_admin.ui.dashboard.ActivityItem
import com.swadratna.swadratna_admin.ui.dashboard.StoreItem

data class DashboardResponse(
    val totalCampaigns: Int,
    val campaignsChange: String,
    val activeStore: Int,
    val storeChange: String,
    val topSeller: String,
    val topSellerMetric: String,
    val newUsers: Int,
    val newUsersChange: String,
    val recentActivities: List<ActivityItemResponse>,
    val topStore: List<StoreItemResponse>
)

data class ActivityItemResponse(
    val title: String,
    val time: String
)

data class StoreItemResponse(
    val name: String,
    val revenue: String
)
