package com.swadratna.swadratna_admin.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swadratna.swadratna_admin.data.repository.DashboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: DashboardRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val response = repository.getDashboardData()

                _uiState.value = response.toUiState().copy(
                    isLoading = false,
                    error = null
                )

            } catch (e: Exception) {
                _uiState.value = DashboardUiState(
                    isLoading = false,
                    error = e.message ?: "Unknown error"
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