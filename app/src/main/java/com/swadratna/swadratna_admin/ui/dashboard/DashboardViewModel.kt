package com.swadratna.swadratna_admin.ui.dashboard

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import kotlin.math.roundToInt
import androidx.lifecycle.viewModelScope
import com.swadratna.swadratna_admin.data.repository.ActivityRepository
import com.swadratna.swadratna_admin.data.repository.AnalyticsRepository
import com.swadratna.swadratna_admin.data.repository.CampaignRepository
import com.swadratna.swadratna_admin.data.repository.DashboardRepository
import com.swadratna.swadratna_admin.data.repository.SalesRepository
import com.swadratna.swadratna_admin.data.repository.StoreRepository
import com.swadratna.swadratna_admin.data.wrapper.Result
import com.swadratna.swadratna_admin.utils.ApiConstants
import com.swadratna.swadratna_admin.utils.SharedPrefsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.onSuccess

@HiltViewModel
@RequiresApi(Build.VERSION_CODES.O)
class DashboardViewModel @Inject constructor(
    private val dashboardRepository: DashboardRepository,
    private val analyticsRepository: AnalyticsRepository,
    private val activityRepository: ActivityRepository,
    private val campaignRepository: CampaignRepository,
    private val storeRepository: StoreRepository,
    private val salesRepository: SalesRepository,
    private val sharedPrefsManager: SharedPrefsManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
        fetchSalesData()
    }

    fun refreshDashboardData() {
        loadDashboardData()
        fetchSalesData()
    }

    private fun fetchSalesData() {
        viewModelScope.launch {
            val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
            salesRepository.getSales(date = today, fromDate = null, toDate = null, locationIds = null).collect { result ->
                if (result is Result.Success) {
                    val response = result.data
                    val total = response.summary?.totalAmount ?: 0.0
                    
                    // Sales change logic
                    val lastDate = sharedPrefsManager.getLastRecordedDate()
                    val lastRecordedSales = sharedPrefsManager.getLastRecordedSales()
                    var baselineSales = sharedPrefsManager.getSalesBaseline()

                    // If date has changed since last record, promote last sales to baseline
                    if (lastDate != null && lastDate != today) {
                        baselineSales = lastRecordedSales
                        sharedPrefsManager.saveSalesBaseline(baselineSales)
                        sharedPrefsManager.saveLastRecordedDate(today)
                    } else if (lastDate == null) {
                        // First run ever
                        sharedPrefsManager.saveLastRecordedDate(today)
                    }

                    // Calculate percentage
                    val percentText = if (baselineSales > 0) {
                        val diff = total - baselineSales
                        val pct = ((diff / baselineSales) * 100).roundToInt()
                        val sign = if (pct > 0) "+" else ""
                        "$sign$pct% changes"
                    } else if (baselineSales == 0.0 && total > 0) {
                        "100% changes"
                    } else {
                        "0% changes"
                    }

                    // Always update current sales as "last recorded" for tomorrow's baseline
                    sharedPrefsManager.saveLastRecordedSales(total)

                    _uiState.update { it.copy(totalSales = total.toString(), salesChange = percentText) }
                }
            }
        }
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val response = dashboardRepository.getDashboardData()
                val analytics = analyticsRepository.loadDashboard(null, null, null)
                val totalReferrals = analytics.referralStats?.totalReferrals ?: 0

                val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
                val lastDate = sharedPrefsManager.getLastRecordedNewUsersDate()
                val lastRecordedNewUsers = sharedPrefsManager.getLastRecordedNewUsers()
                var baselineNewUsers = sharedPrefsManager.getNewUsersBaseline()

                if (lastDate == null) {
                    baselineNewUsers = totalReferrals
                    sharedPrefsManager.saveNewUsersBaseline(baselineNewUsers)
                    sharedPrefsManager.saveLastRecordedNewUsersDate(today)
                } else if (lastDate != today) {
                    baselineNewUsers = lastRecordedNewUsers
                    sharedPrefsManager.saveNewUsersBaseline(baselineNewUsers)
                    sharedPrefsManager.saveLastRecordedNewUsersDate(today)
                }
                
                sharedPrefsManager.saveLastRecordedNewUsers(totalReferrals)

                val newUsersPercentText = if (baselineNewUsers > 0) {
                    val diff = totalReferrals - baselineNewUsers
                    if (diff == 0) {
                        "0% changes"
                    } else {
                        val pct = ((diff.toDouble() / baselineNewUsers) * 100).roundToInt()
                        val sign = if (pct > 0) "+" else ""
                        "$sign$pct% changes"
                    }
                } else if (baselineNewUsers == 0 && totalReferrals > 0) {
                    "100% changes"
                } else {
                    "0% changes"
                }

                _uiState.update {
                    it.copy(
                        topSeller = response.topSeller,
                        topSellerMetric = response.topSellerMetric,
                        newUsers = totalReferrals, // Use totalReferrals from analytics
                        newUsersChange = newUsersPercentText,
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
                    val count = res.data.campaigns?.size ?: 0
                    val prev = sharedPrefsManager.getPrevTotalCampaigns()
                    val percentText = if (prev == null) {
                        sharedPrefsManager.savePrevTotalCampaigns(count)
                        "100 % changes since last month"
                    } else {
                        val diff = count - prev
                        val pct = if (prev == 0) 100 else ((diff.toDouble() / prev.toDouble()) * 100).roundToInt()
                        "${pct} % changes since last month"
                    }
                    _uiState.update { it.copy(totalCampaigns = count, campaignsChange = percentText) }
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
            val restaurantId = ApiConstants.RESTAURANT_ID
            val result = storeRepository.getStores(page = 1, limit = 1000, restaurantId = restaurantId)
            result.onSuccess { resp ->
                val activeCount = resp.stores.count { it.status.equals("active", ignoreCase = true) }
                sharedPrefsManager.saveStores(resp.stores)
                val prev = sharedPrefsManager.getPrevActiveStores()
                val hasLocalStores = sharedPrefsManager.getStores().isNotEmpty()
                val percentText = if (prev == null || !hasLocalStores) {
                    sharedPrefsManager.savePrevActiveStores(activeCount)
                    "100 % changes since last 3 months"
                } else {
                    val diff = activeCount - prev
                    val pct = if (prev == 0) 100 else ((diff.toDouble() / prev.toDouble()) * 100).roundToInt()
                    "${pct} % changes since last 3 months"
                }
                val topSellerName = if (resp.stores.size == 1) resp.stores.first().name else "N/A"
                _uiState.update { it.copy(activeStore = activeCount, storeChange = percentText, topSeller = topSellerName) }
            }.onFailure {
                val cachedStores = sharedPrefsManager.getStores()
                val activeCount = cachedStores.count { it.status.equals("active", ignoreCase = true) }
                val prev = sharedPrefsManager.getPrevActiveStores()
                val percentText = if (prev == null || cachedStores.isEmpty()) {
                    "100 % changes since last 3 months"
                } else {
                    val diff = activeCount - prev
                    val pct = if (prev == 0) 100 else ((diff.toDouble() / prev.toDouble()) * 100).roundToInt()
                    "${pct} % changes since last 3 months"
                }
                val topSellerName = if (cachedStores.size == 1) cachedStores.first().name else "N/A"
                _uiState.update { it.copy(activeStore = activeCount, storeChange = percentText, topSeller = topSellerName) }
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
    val error: String? = null,
    val totalSales: String = "0",
    val salesChange: String = ""
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