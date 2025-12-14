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
    val totalHours: Double
)

@HiltViewModel
class AttendanceViewModel @Inject constructor(
    private val repository: AttendanceRepository
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
                        val aggregated = calculateAggregation(result.data)
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

    private fun calculateAggregation(response: AttendanceResponse): List<StaffAttendanceAggregate> {
        val allStaffEntries = response.attendance.flatMap { it.staff }
        
        return allStaffEntries
            .groupBy { it.staffId }
            .map { (id, entries) ->
                val name = entries.firstOrNull()?.name ?: "Unknown"
                StaffAttendanceAggregate(
                    staffId = id,
                    name = name,
                    presentCount = entries.count { it.status.equals("present", ignoreCase = true) },
                    absentCount = entries.count { it.status.equals("absent", ignoreCase = true) },
                    halfDayCount = entries.count { it.status.equals("half_day", ignoreCase = true) },
                    leaveCount = entries.count { it.status.equals("on_leave", ignoreCase = true) },
                    totalHours = entries.sumOf { it.totalHours }
                )
            }
            .sortedBy { it.name }
    }
}
