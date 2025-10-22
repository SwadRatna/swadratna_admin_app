package com.swadratna.swadratna_admin.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swadratna.swadratna_admin.data.repository.ActivityRepository
import com.swadratna.swadratna_admin.data.repository.DashboardRepository
import com.swadratna.swadratna_admin.data.repository.CampaignRepository
import com.swadratna.swadratna_admin.data.repository.StoreRepository
import com.swadratna.swadratna_admin.utils.SharedPrefsManager
import com.swadratna.swadratna_admin.data.wrapper.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val dashboardRepository: DashboardRepository,
    private val activityRepository: ActivityRepository,
    private val campaignRepository: CampaignRepository,
    private val storeRepository: StoreRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        // Load dashboard summary cards from backend
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val response = dashboardRepository.getDashboardData()
                _uiState.update {
                    it.copy(
                        campaignsChange = "0 % changes since last month",
                        storeChange = "0 % changes since last 3 months",
                        topSeller = response.topSeller,
                        topSellerMetric = response.topSellerMetric,
                        newUsers = response.newUsers,
                        newUsersChange = response.newUsersChange,
                        topStore = response.topStore.map { StoreItem(name = it.name, revenue = it.revenue) },
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }

        viewModelScope.launch {
            when (val res = campaignRepository.adminListCampaigns(status = null, type = null, search = null, page = null, limit = 1000)) {
                is Result.Success -> {
                    val count = res.data.campaigns.size
                    _uiState.update { it.copy(totalCampaigns = count) }
                }
                is Result.Error -> {
                    // Keep existing value on error
                }
                is Result.Loading -> {
                    // no-op
                }
            }
        }

        viewModelScope.launch {
            val restaurantId = 1000001 // TODO: replace with dynamic restaurant id
            val result = storeRepository.getStores(page = 1, limit = 1000, restaurantId = restaurantId)
            result.onSuccess { resp ->
                val activeCount = resp.stores.count { it.status.equals("active", ignoreCase = true) }
                _uiState.update { it.copy(activeStore = activeCount) }
            }.onFailure {
                // Keep existing value on error
            }
        }

        viewModelScope.launch {
            activityRepository.getRecentActivities(3).collect { recentActivities ->
                val activityItems = recentActivities.map { activity ->
                    ActivityItem(
                        title = activity.title,
                        time = activity.getFormattedTime()
                    )
                }
                _uiState.update { it.copy(recentActivities = activityItems) }
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
    val topStore: List<StoreItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
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