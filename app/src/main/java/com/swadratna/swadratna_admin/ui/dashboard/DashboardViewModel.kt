package com.swadratna.swadratna_admin.ui.dashboard

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        // Initialize with mock data
        _uiState.value = DashboardUiState(
            searchQuery = "",
            totalCampaigns = 124,
            campaignsChange = "+12% since last month",
            activeStore = 89,
            storeChange = "+5% last 3 months",
            topSeller = "Burger King",
            topSellerMetric = "30% of total sales",
            newUsers = 450,
            newUsersChange = "-8% compared to avg.",
            recentActivities = listOf(
                ActivityItem(
                    title = "New campaign \"Summer Blast\" launched",
                    time = "2 hours ago"
                ),
                ActivityItem(
                    title = "Store \"Downtown Deli\" updated menu",
                    time = "1 day ago"
                ),
                ActivityItem(
                    title = "Report for Q3 2023 generated",
                    time = "3 days ago"
                )
            ),
            topStore = listOf(
                StoreItem(
                    name = "Coastal Grill",
                    revenue = "$125,000"
                ),
                StoreItem(
                    name = "Urban Eatery",
                    revenue = "$110,500"
                ),
                StoreItem(
                    name = "Parkside Bistro",
                    revenue = "$98,200"
                )
            )
        )
    }

    fun handleEvent(event: DashboardEvent) {
        when (event) {
            is DashboardEvent.SearchQueryChanged -> {
                _uiState.value = _uiState.value.copy(searchQuery = event.query)
            }
            DashboardEvent.RefreshData -> {
                // TODO: Implement refresh logic
            }
        }
    }
}

data class DashboardUiState(
    val searchQuery: String = "",
    val totalCampaigns: Int = 0,
    val campaignsChange: String = "",
    val activeStore: Int = 0,
    val storeChange: String = "",
    val topSeller: String = "",
    val topSellerMetric: String = "",
    val newUsers: Int = 0,
    val newUsersChange: String = "",
    val recentActivities: List<ActivityItem> = emptyList(),
    val topStore: List<StoreItem> = emptyList()
)

data class ActivityItem(
    val title: String,
    val time: String
)

data class StoreItem(
    val name: String,
    val revenue: String
)

sealed interface DashboardEvent {
    data class SearchQueryChanged(val query: String) : DashboardEvent
    object RefreshData : DashboardEvent
}