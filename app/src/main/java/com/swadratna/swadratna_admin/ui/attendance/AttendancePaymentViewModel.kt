package com.swadratna.swadratna_admin.ui.attendance

import androidx.lifecycle.ViewModel
import com.swadratna.swadratna_admin.model.AttendanceStatus
import com.swadratna.swadratna_admin.model.AttendanceSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class AttendancePaymentViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(AttendancePaymentUiState())
    val uiState: StateFlow<AttendancePaymentUiState> = _uiState.asStateFlow()

    init {
        _uiState.value = AttendancePaymentUiState(
            summary = AttendanceSummary(
                totalStaff = 4,
                presentCount = 2,
                absentCount = 1,
                leaveCount = 1
            ),
            attendanceRecords = listOf(
                AttendanceRecord(
                    id = "1",
                    name = "Alice Johnson",
                    position = "Marketing Specialist",
                    status = AttendanceStatus.PRESENT,
                    dailyWage = 120.0
                ),
                AttendanceRecord(
                    id = "2",
                    name = "Robert Smith",
                    position = "Software Engineer",
                    status = AttendanceStatus.ABSENT,
                    dailyWage = 150.0
                ),
                AttendanceRecord(
                    id = "3",
                    name = "Emily Chen",
                    position = "Project Manager",
                    status = AttendanceStatus.PRESENT,
                    dailyWage = 135.0
                ),
                AttendanceRecord(
                    id = "4",
                    name = "David Lee",
                    position = "Data Analyst",
                    status = AttendanceStatus.LEAVE,
                    dailyWage = 110.0
                )
            )
        )
    }

    fun updateWage(staffId: String, wage: Double) {
        val updatedRecords = _uiState.value.attendanceRecords.map { record ->
            if (record.id == staffId) {
                record.copy(dailyWage = wage)
            } else {
                record
            }
        }
        _uiState.update { it.copy(attendanceRecords = updatedRecords) }
    }
}

data class AttendancePaymentUiState(
    val summary: AttendanceSummary = AttendanceSummary(0, 0, 0, 0),
    val attendanceRecords: List<AttendanceRecord> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class AttendanceRecord(
    val id: String,
    val name: String,
    val position: String,
    val status: AttendanceStatus,
    val dailyWage: Double
)