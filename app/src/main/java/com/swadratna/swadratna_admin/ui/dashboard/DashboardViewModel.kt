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
import com.swadratna.swadratna_admin.data.repository.RestaurantRepository
import com.swadratna.swadratna_admin.data.model.RestaurantProfileRequest
import com.swadratna.swadratna_admin.data.repository.SalesRepository
import com.swadratna.swadratna_admin.data.repository.StoreRepository
import com.swadratna.swadratna_admin.data.repository.InventoryRepository
import com.swadratna.swadratna_admin.data.repository.AttendanceRepository
import com.swadratna.swadratna_admin.data.repository.StaffRepository
import com.swadratna.swadratna_admin.data.model.Ingredient
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.filter
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

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import java.util.Calendar
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale

@HiltViewModel
@RequiresApi(Build.VERSION_CODES.O)
class DashboardViewModel @Inject constructor(
    private val dashboardRepository: DashboardRepository,
    private val analyticsRepository: AnalyticsRepository,
    private val activityRepository: ActivityRepository,
    private val campaignRepository: CampaignRepository,
    private val storeRepository: StoreRepository,
    private val restaurantRepository: RestaurantRepository,
    private val salesRepository: SalesRepository,
    private val inventoryRepository: InventoryRepository,
    private val attendanceRepository: AttendanceRepository,
    private val staffRepository: StaffRepository,
    private val sharedPrefsManager: SharedPrefsManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
        fetchSalesData()
        loadLowStock()
    }

    fun refreshDashboardData() {
        loadDashboardData()
        fetchSalesData()
        loadLowStock(prompt = true)
    }

    fun updateRestaurantProfile(request: RestaurantProfileRequest, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProfileUpdating = true) }
            val result = restaurantRepository.updateRestaurantProfile(ApiConstants.RESTAURANT_ID, request)
            _uiState.update { it.copy(isProfileUpdating = false) }
            
            when (result) {
                is Result.Success -> onSuccess()
                is Result.Error -> onError(result.throwable?.message ?: "Unknown error")
                is Result.Loading -> {}
            }
        }
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
        Log.d("DashboardViewModel", "loadDashboardData called")
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                Log.d("DashboardViewModel", "Fetching dashboard stats...")
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
                        // topStore = response.topStore.map { StoreItem(name = it.name, revenue = it.revenue) }, // Don't overwrite topStore with potential mock data
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
            Log.d("DashboardViewModel", "Fetching stores for restaurantId: $restaurantId")
            val result = storeRepository.getStores(page = 1, limit = 1000, restaurantId = restaurantId)
            result.onSuccess { resp ->
                val stores = resp.stores ?: emptyList()
                Log.d("DashboardViewModel", "Stores fetch success. Count: ${stores.size}")
                val activeCount = stores.count { it.status.equals("active", ignoreCase = true) }
                sharedPrefsManager.saveStores(stores)
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

                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val calendar = Calendar.getInstance()
                val today = dateFormat.format(calendar.time)
                
                calendar.add(Calendar.DAY_OF_YEAR, -7)
                val fromDate = dateFormat.format(calendar.time)

                val storeSalesDeferred = stores.map { store ->
                    async {
                        var revenue = 0.0
                        try {
                            Log.d("DashboardViewModel", "Fetching sales for store: ${store.name} (ID: ${store.id})")
                            val salesResult = salesRepository.getSales(
                                date = null, 
                                fromDate = fromDate, 
                                toDate = today, 
                                locationIds = store.id.toString()
                            ).filter { it !is Result.Loading }.first()

                            if (salesResult is Result.Success) {
                                revenue = salesResult.data.summary?.totalAmount ?: 0.0
                                Log.d("DashboardViewModel", "Sales fetched for ${store.name}: $revenue")
                            } else if (salesResult is Result.Error) {
                                Log.e("DashboardViewModel", "Error fetching sales for ${store.name}: ${salesResult.throwable?.message}")
                            }
                        } catch (e: Exception) {
                            Log.e("DashboardViewModel", "Exception in sales fetch for ${store.name}", e)
                        }
                        store to revenue
                    }
                }

                val storeSales = storeSalesDeferred.awaitAll()

                val topPerformingStores = storeSales
                    .sortedByDescending { it.second }
                    .take(3)
                    .map { (store, revenue) ->
                        StoreItem(
                            name = store.name,
                            revenue = "₹ ${String.format("%.2f", revenue)}"
                        )
                    }
                Log.d("DashboardViewModel", "Top performers count: ${topPerformingStores.size}")

                val topSellerName = if (topPerformingStores.isNotEmpty()) topPerformingStores.first().name else "N/A"

                _uiState.update { 
                    it.copy(
                        activeStore = activeCount, 
                        storeChange = percentText, 
                        topSeller = topSellerName,
                        topStore = topPerformingStores 
                    ) 
                }
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
                
                // Populate Top Stores from cache (with 0 revenue as fallback) to avoid "No data available"
                val topPerformingStores = cachedStores.take(3).map { store ->
                    StoreItem(
                        name = store.name,
                        revenue = "₹ 0.00" // Fallback revenue
                    )
                }
                 
                _uiState.update { 
                    it.copy(
                        activeStore = activeCount, 
                        storeChange = percentText, 
                        topSeller = topSellerName,
                        topStore = topPerformingStores
                    ) 
                }
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

    private fun loadLowStock(prompt: Boolean = false) {
        viewModelScope.launch {
            val prev = _uiState.value.lowStock
            val result = inventoryRepository.getLowStock()
            result.fold(
                onSuccess = { items ->
                    val shouldPrompt = if (prompt) true else prev.isEmpty() && items.isNotEmpty()
                    _uiState.update { it.copy(lowStock = items, shouldPromptLowStock = shouldPrompt) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
            )
        }
    }

    fun onLowStockDialogDismissed() {
        _uiState.update { it.copy(shouldPromptLowStock = false) }
    }

    fun loadOverallReport(period: String) {
        viewModelScope.launch {
            val (fromDate, toDate) = computeRange(period)
            var salesTotal = 0.0
            try {
                val salesResult = salesRepository.getSales(
                    date = null,
                    fromDate = fromDate,
                    toDate = toDate,
                    locationIds = null
                ).filter { it !is Result.Loading }.first()
                if (salesResult is Result.Success) {
                    salesTotal = salesResult.data.summary?.totalAmount ?: 0.0
                }
            } catch (_: Exception) {}

            var inventoryCost = 0.0
            try {
                val usageResult = inventoryRepository.getUsage(period = period)
                usageResult.fold(
                    onSuccess = { (_, totals) -> inventoryCost = totals?.totalCost ?: 0.0 },
                    onFailure = { }
                )
            } catch (_: Exception) {}

            var salaryTotal = 0.0
            try {
                val restaurantId = ApiConstants.RESTAURANT_ID
                val storesRes = storeRepository.getStores(page = 1, limit = 1000, restaurantId = restaurantId)
                storesRes.onSuccess { resp ->
                    val stores = resp.stores ?: emptyList()
                    stores.forEach { store ->
                        val attRes = attendanceRepository.getAttendance(fromDate, toDate, store.id?.toString() ?: "")
                        when (attRes) {
                            is Result.Success -> {
                                val attendance = attRes.data
                                val staffList = staffRepository.getStaff(store.id ?: 0).getOrNull()?.staff ?: emptyList()
                                salaryTotal += calculateTotalSalary(attendance, staffList)
                            }
                            is Result.Error -> { }
                            is Result.Loading -> { }
                        }
                    }
                }
            } catch (_: Exception) {}

            val extras = _uiState.value.overallExtras
            val finalEarning = salesTotal - (salaryTotal + inventoryCost + extras)

            _uiState.update {
                it.copy(
                    overallPeriodLabel = period,
                    overallSalesTotal = salesTotal,
                    overallSalaryTotal = salaryTotal,
                    overallInventoryCostTotal = inventoryCost,
                    overallFinalEarning = finalEarning,
                    showOverallReportDialog = true
                )
            }
        }
    }

    fun updateOverallExtras(value: Double) {
        _uiState.update { state ->
            val final = state.overallSalesTotal - (state.overallSalaryTotal + state.overallInventoryCostTotal + value)
            state.copy(overallExtras = value, overallFinalEarning = final)
        }
    }

    fun dismissOverallReportDialog() {
        _uiState.update { it.copy(showOverallReportDialog = false) }
    }

    private fun computeRange(period: String): Pair<String, String> {
        val today = java.time.LocalDate.now()
        return if (period.equals("weekly", true)) {
            val from = today.minusDays(6)
            from.toString() to today.toString()
        } else {
            val from = today.withDayOfMonth(1)
            from.toString() to today.toString()
        }
    }

    private fun calculateTotalSalary(
        response: com.swadratna.swadratna_admin.data.model.AttendanceResponse,
        staffList: List<com.swadratna.swadratna_admin.data.model.Staff>
    ): Double {
        val allEntriesWithDate = response.attendance.flatMap { day ->
            day.staff.map { staffEntry ->
                Pair(day.date, staffEntry)
            }
        }
        return allEntriesWithDate
            .groupBy { it.second.staffId }
            .map { (id, entriesWithDate) ->
                val staff = staffList.find { it.id == id }
                val monthlySalary = staff?.salary ?: 0.0
                var totalSalary = 0.0
                if (monthlySalary > 0) {
                    entriesWithDate.forEach { (dateStr, entry) ->
                        try {
                            val date = java.time.LocalDate.parse(dateStr.take(10))
                            val daysInMonth = date.lengthOfMonth()
                            val dailySalary = monthlySalary / daysInMonth.toDouble()
                            if (entry.status.equals("present", true)) {
                                totalSalary += dailySalary
                            } else if (entry.status.equals("half_day", true)) {
                                totalSalary += (dailySalary * 0.5)
                            }
                        } catch (_: Exception) {
                            val dailySalary = monthlySalary / 30.0
                            if (entry.status.equals("present", true)) {
                                totalSalary += dailySalary
                            } else if (entry.status.equals("half_day", true)) {
                                totalSalary += (dailySalary * 0.5)
                            }
                        }
                    }
                }
                totalSalary
            }
            .sum()
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
    val salesChange: String = "",
    val isProfileUpdating: Boolean = false
    , val lowStock: List<Ingredient> = emptyList()
    , val shouldPromptLowStock: Boolean = false
    , val overallPeriodLabel: String = ""
    , val overallSalesTotal: Double = 0.0
    , val overallSalaryTotal: Double = 0.0
    , val overallInventoryCostTotal: Double = 0.0
    , val overallExtras: Double = 0.0
    , val overallFinalEarning: Double = 0.0
    , val showOverallReportDialog: Boolean = false
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
