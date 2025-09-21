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
    
    private val _allStaff = mutableListOf<Staff>()

    init {
        val initialStaffList = listOf(
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
        
        _allStaff.clear()
        _allStaff.addAll(initialStaffList)
        
        _uiState.value = StaffManagementUiState(
            searchQuery = "",
            staffList = initialStaffList
        )
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFiltersAndSort()
    }

    fun editStaff(staffId: String) {
        // TODO: Implement edit staff functionality
    }

    fun deleteStaff(staffId: String) {
        _allStaff.removeIf { it.id == staffId }
        applyFiltersAndSort()
    }
    
    fun updateFilter(filter: String?) {
        _uiState.update { it.copy(selectedFilter = filter, isFilterMenuVisible = false) }
        applyFiltersAndSort()
    }
    
    fun updateSortOrder(sortOrder: String) {
        _uiState.update { it.copy(selectedSortOrder = sortOrder, isSortMenuVisible = false) }
        applyFiltersAndSort()
    }
    
    private fun applyFiltersAndSort() {
        val searchQuery = _uiState.value.searchQuery.lowercase()
        val selectedFilter = _uiState.value.selectedFilter
        val sortOrder = _uiState.value.selectedSortOrder
        
        // First apply filters
        val filteredStaff = _allStaff.filter { staff ->
            // Apply search filter
            val matchesSearch = searchQuery.isEmpty() || 
                staff.name.lowercase().contains(searchQuery) || 
                staff.position.lowercase().contains(searchQuery)
            
            // Apply status filter
            val matchesStatus = selectedFilter == null || staff.status.name == selectedFilter
            
            matchesSearch && matchesStatus
        }
        
        // Then sort the filtered list
        val sortedStaff = when (sortOrder) {
            "NAME_ASC" -> filteredStaff.sortedBy { it.name }
            "NAME_DESC" -> filteredStaff.sortedByDescending { it.name }
            "POSITION_ASC" -> filteredStaff.sortedBy { it.position }
            "POSITION_DESC" -> filteredStaff.sortedByDescending { it.position }
            else -> filteredStaff
        }
        
        _uiState.update { it.copy(staffList = sortedStaff) }
    }

    fun onEvent(event: StaffEvent) {
        when (event) {
            is StaffEvent.ToggleFilterMenu -> {
                _uiState.value = _uiState.value.copy(
                    isFilterMenuVisible = !_uiState.value.isFilterMenuVisible,
                    isSortMenuVisible = false
                )
            }
            is StaffEvent.ToggleSortMenu -> {
                _uiState.value = _uiState.value.copy(
                    isSortMenuVisible = !_uiState.value.isSortMenuVisible,
                    isFilterMenuVisible = false
                )
            }
        }
    }
}

data class StaffManagementUiState(
    val searchQuery: String = "",
    val staffList: List<Staff> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isFilterMenuVisible: Boolean = false,
    val isSortMenuVisible: Boolean = false,
    val selectedFilter: String? = null,
    val selectedSortOrder: String = "NAME_ASC"
)

sealed interface StaffEvent {
    object ToggleFilterMenu : StaffEvent
    object ToggleSortMenu : StaffEvent
}
