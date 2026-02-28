package com.swadratna.swadratna_admin.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swadratna.swadratna_admin.data.repository.AttendanceRepository
import com.swadratna.swadratna_admin.data.repository.InventoryRepository
import com.swadratna.swadratna_admin.data.repository.SalesRepository
import com.swadratna.swadratna_admin.data.repository.StaffRepository
import com.swadratna.swadratna_admin.data.repository.StoreRepository
import com.swadratna.swadratna_admin.data.wrapper.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ExtraItem(
    val name: String,
    val value: Double,
    val operation: String
)

data class OverallReportUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val fromDate: String = java.time.LocalDate.now().withDayOfMonth(1).toString(),
    val toDate: String = java.time.LocalDate.now().toString(),
    val salesTotal: Double = 0.0,
    val salaryTotal: Double = 0.0,
    val inventoryTotal: Double = 0.0,
    val extras: List<ExtraItem> = emptyList(),
    val finalEarning: Double = 0.0
)

@HiltViewModel
class OverallReportViewModel @Inject constructor(
    private val salesRepository: SalesRepository,
    private val inventoryRepository: InventoryRepository,
    private val attendanceRepository: AttendanceRepository,
    private val staffRepository: StaffRepository,
    private val storeRepository: StoreRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(OverallReportUiState())
    val uiState: StateFlow<OverallReportUiState> = _uiState

    init {
        loadReport()
    }

    fun setDateRange(from: String, to: String) {
        _uiState.update { it.copy(fromDate = from, toDate = to) }
        loadReport()
    }

    fun quickPeriod(period: String) {
        val today = java.time.LocalDate.now()
        if (period.equals("weekly", true)) {
            val from = today.minusDays(6).toString()
            setDateRange(from, today.toString())
        } else {
            val from = today.withDayOfMonth(1).toString()
            setDateRange(from, today.toString())
        }
    }

    fun addExtra(name: String, value: Double, operation: String) {
        val list = _uiState.value.extras.toMutableList()
        list.add(ExtraItem(name, value, operation))
        _uiState.update { it.copy(extras = list) }
        recomputeFinal()
    }

    fun removeExtra(index: Int) {
        val list = _uiState.value.extras.toMutableList()
        if (index in list.indices) {
            list.removeAt(index)
            _uiState.update { it.copy(extras = list) }
            recomputeFinal()
        }
    }

    private fun loadReport() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val from = _uiState.value.fromDate
            val to = _uiState.value.toDate
            var sales = 0.0
            var inventory = 0.0
            var salary = 0.0

            try {
                val res = salesRepository.getSales(date = null, fromDate = from, toDate = to, locationIds = null)
                val r = res.filter { it !is Result.Loading }.first()
                if (r is Result.Success) {
                    sales = r.data.summary?.totalAmount ?: 0.0
                }
            } catch (_: Exception) {}

            try {
                val usage = inventoryRepository.getUsage(period = "custom", startDate = from, endDate = to, type = "all")
                usage.fold(
                    onSuccess = { (_, totals) -> inventory = totals?.totalCost ?: 0.0 },
                    onFailure = { }
                )
            } catch (_: Exception) {}

            try {
                val restaurantId = com.swadratna.swadratna_admin.utils.ApiConstants.RESTAURANT_ID
                val storesRes = storeRepository.getStores(page = 1, limit = 1000, restaurantId = restaurantId)
                storesRes.onSuccess { resp ->
                    val stores = resp.stores ?: emptyList()
                    stores.forEach { store ->
                        val att = attendanceRepository.getAttendance(from, to, store.id?.toString() ?: "")
                        if (att is Result.Success) {
                            val staffList = staffRepository.getStaff(store.id ?: 0).getOrNull()?.staff ?: emptyList()
                            salary += calculateTotalSalary(att.data, staffList)
                        }
                    }
                }
            } catch (_: Exception) {}

            _uiState.update { it.copy(salesTotal = sales, salaryTotal = salary, inventoryTotal = inventory, isLoading = false) }
            recomputeFinal()
        }
    }

    private fun recomputeFinal() {
        val s = _uiState.value
        val add = s.extras.filter { it.operation.equals("add", true) }.sumOf { it.value }
        val sub = s.extras.filter { it.operation.equals("subtract", true) }.sumOf { it.value }
        val final = s.salesTotal - (s.salaryTotal + s.inventoryTotal) + add - sub
        _uiState.update { it.copy(finalEarning = final) }
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
