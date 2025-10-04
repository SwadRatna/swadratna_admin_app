package com.swadratna.swadratna_admin.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swadratna.swadratna_admin.data.repository.ActivityRepository
import com.swadratna.swadratna_admin.data.repository.DashboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val dashboardRepository: DashboardRepository,
    private val activityRepository: ActivityRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            // Load recent activities from ActivityRepository
            activityRepository.getRecentActivities(3).collect { recentActivities ->
                val activityItems = recentActivities.map { activity ->
                    ActivityItem(
                        title = activity.title,
                        time = activity.getFormattedTime()
                    )
                }
                
                _uiState.value = _uiState.value.copy(
                     recentActivities = activityItems
                 )
            }
        }
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