package com.swadratna.swadratna_admin.ui.dashboard

import com.swadratna.swadratna_admin.data.model.ActivityItemResponse
import com.swadratna.swadratna_admin.data.model.DashboardResponse
import com.swadratna.swadratna_admin.data.model.StoreItemResponse

fun DashboardResponse.toUiState(): DashboardUiState {
    return DashboardUiState(
        searchQuery = "",
        totalCampaigns = totalCampaigns,
        campaignsChange = campaignsChange,
        activeStore = activeStore,
        storeChange = storeChange,
        topSeller = topSeller,
        topSellerMetric = topSellerMetric,
        newUsers = newUsers,
        newUsersChange = newUsersChange,
        recentActivities = recentActivities.map { it.toUiModel() },
        topStore = topStore.map { it.toUiModel() }
    )
}

fun ActivityItemResponse.toUiModel(): ActivityItem {
    return ActivityItem(title = title, time = time)
}

fun StoreItemResponse.toUiModel(): StoreItem {
    return StoreItem(name = name, revenue = revenue)
}
