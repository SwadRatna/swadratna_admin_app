package com.swadratna.swadratna_admin.ui.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swadratna.swadratna_admin.data.model.AttendanceResponse
import com.swadratna.swadratna_admin.data.repository.AttendanceRepository
import com.swadratna.swadratna_admin.data.wrapper.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class AttendanceUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val attendanceResponse: AttendanceResponse? = null,
    val fromDate: String = LocalDate.now().toString(),
    val toDate: String = LocalDate.now().toString(),
    val aggregatedStaff: List<StaffAttendanceAggregate> = emptyList()
)

data class StaffAttendanceAggregate(
    val staffId: Int,
    val name: String,
    val presentCount: Int,
    val absentCount: Int,
    val halfDayCount: Int,
    val leaveCount: Int,
    val totalHours: Double,
    val calculatedSalary: Double
)

@HiltViewModel
class AttendanceViewModel @Inject constructor(
    private val repository: AttendanceRepository,
    private val staffRepository: com.swadratna.swadratna_admin.data.repository.StaffRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AttendanceUiState())
    val uiState: StateFlow<AttendanceUiState> = _uiState.asStateFlow()

    fun updateDateRange(from: String, to: String) {
        _uiState.update { it.copy(fromDate = from, toDate = to) }
    }

    fun fetchAttendance(storeId: String) {
        viewModelScope.launch {
            val currentState = _uiState.value
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val result = repository.getAttendance(
                currentState.fromDate,
                currentState.toDate,
                storeId
            )
            
            _uiState.update { state ->
                when (result) {
                    is Result.Success -> {
                        val storeIdInt = storeId.toIntOrNull() ?: 0
                        val staffList = staffRepository.getStaff(storeIdInt)
                            .getOrNull()?.staff ?: emptyList()
                            
                        val aggregated = calculateAggregation(result.data, staffList)
                        state.copy(
                            isLoading = false,
                            attendanceResponse = result.data,
                            aggregatedStaff = aggregated
                        )
                    }
                    is Result.Error -> state.copy(
                        isLoading = false,
                        error = result.message
                    )
                    Result.Loading -> state.copy(isLoading = true)
                }
            }
        }
    }

    private fun calculateAggregation(
        response: AttendanceResponse,
        staffList: List<com.swadratna.swadratna_admin.data.model.Staff>
    ): List<StaffAttendanceAggregate> {
        // Flatten to get all staff entries but keep the date context for salary calculation
        val allEntriesWithDate = response.attendance.flatMap { day ->
            day.staff.map { staffEntry ->
                Pair(day.date, staffEntry)
            }
        }
        
        return allEntriesWithDate
            .groupBy { it.second.staffId }
            .map { (id, entriesWithDate) ->
                val entries = entriesWithDate.map { it.second }
                val name = entries.firstOrNull()?.name ?: "Unknown"
                val presentCount = entries.count { it.status.equals("present", ignoreCase = true) }
                val absentCount = entries.count { it.status.equals("absent", ignoreCase = true) }
                val halfDayCount = entries.count { it.status.equals("half_day", ignoreCase = true) }
                val leaveCount = entries.count { it.status.equals("on_leave", ignoreCase = true) }
                
                // Calculate salary dynamically based on days in month
                val staff = staffList.find { it.id == id }
                val monthlySalary = staff?.salary ?: 0.0
                
                var totalSalary = 0.0
                
                if (monthlySalary > 0) {
                    entriesWithDate.forEach { (dateStr, entry) ->
                        try {
                            val date = parseDate(dateStr)
                            val daysInMonth = date.lengthOfMonth()
                            val dailySalary = monthlySalary / daysInMonth.toDouble()
                            
                            if (entry.status.equals("present", ignoreCase = true)) {
                                totalSalary += dailySalary
                            } else if (entry.status.equals("half_day", ignoreCase = true)) {
                                totalSalary += (dailySalary * 0.5)
                            }
                        } catch (e: Exception) {
                            // Fallback to 30 days if date parsing fails
                            val dailySalary = monthlySalary / 30.0
                            if (entry.status.equals("present", ignoreCase = true)) {
                                totalSalary += dailySalary
                            } else if (entry.status.equals("half_day", ignoreCase = true)) {
                                totalSalary += (dailySalary * 0.5)
                            }
                        }
                    }
                }
                
                StaffAttendanceAggregate(
                    staffId = id,
                    name = name,
                    presentCount = presentCount,
                    absentCount = absentCount,
                    halfDayCount = halfDayCount,
                    leaveCount = leaveCount,
                    totalHours = entries.sumOf { it.totalHours },
                    calculatedSalary = totalSalary
                )
            }
            .sortedBy { it.name }
    }

    private fun parseDate(dateStr: String): LocalDate {
        return try {
            LocalDate.parse(dateStr)
        } catch (e: Exception) {
            try {
                // Try removing time component if ISO format
                if (dateStr.length >= 10) {
                    LocalDate.parse(dateStr.take(10))
                } else {
                    throw e
                }
            } catch (e2: Exception) {
                try {
                    // Try dd-MM-yyyy
                    LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                } catch (e3: Exception) {
                    try {
                        // Try dd/MM/yyyy
                        LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    } catch (e4: Exception) {
                        throw e4
                    }
                }
            }
        }
    }
}
