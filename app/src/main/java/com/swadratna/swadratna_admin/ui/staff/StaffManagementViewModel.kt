package com.swadratna.swadratna_admin.ui.staff

import androidx.lifecycle.ViewModel
import com.swadratna.swadratna_admin.model.Staff
import com.swadratna.swadratna_admin.model.StaffStatus
import com.swadratna.swadratna_admin.model.WorkingHours
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class StaffManagementViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(StaffManagementUiState())
    val uiState: StateFlow<StaffManagementUiState> = _uiState.asStateFlow()

    init {
        _uiState.value = StaffManagementUiState(
            searchQuery = "",
            staffList = listOf(
                Staff(
                    id = "1",
                    name = "Emily Johnson",
                    position = "Manager",
                    status = StaffStatus.ACTIVE,
                    workingHours = WorkingHours(
                        startTime = LocalTime.of(9, 0),
                        endTime = LocalTime.of(17, 0)
                    )
                ),
                Staff(
                    id = "2",
                    name = "Michael Chen",
                    position = "Chef",
                    status = StaffStatus.ON_BREAK,
                    workingHours = WorkingHours(
                        startTime = LocalTime.of(10, 0),
                        endTime = LocalTime.of(18, 0)
                    )
                ),
                Staff(
                    id = "3",
                    name = "Sarah Williams",
                    position = "Cashier",
                    status = StaffStatus.ACTIVE,
                    workingHours = WorkingHours(
                        startTime = LocalTime.of(8, 0),
                        endTime = LocalTime.of(16, 0)
                    )
                ),
                Staff(
                    id = "4",
                    name = "David Lee",
                    position = "Waiter",
                    status = StaffStatus.ACTIVE,
                    workingHours = WorkingHours(
                        startTime = LocalTime.of(12, 0),
                        endTime = LocalTime.of(20, 0)
                    )
                ),
                Staff(
                    id = "5",
                    name = "Jessica Brown",
                    position = "Cleaner",
                    status = StaffStatus.INACTIVE,
                    workingHours = WorkingHours(
                        startTime = LocalTime.of(11, 0),
                        endTime = LocalTime.of(19, 0)
                    )
                )
            )
        )
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun editStaff(staffId: String) {
        // TODO: Implement edit staff functionality
    }

    fun deleteStaff(staffId: String) {
        // TODO: Implement delete staff functionality
        val updatedList = _uiState.value.staffList.filter { it.id != staffId }
        _uiState.update { it.copy(staffList = updatedList) }
    }
}

data class StaffManagementUiState(
    val searchQuery: String = "",
    val staffList: List<Staff> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)