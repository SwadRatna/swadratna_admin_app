package com.swadratna.swadratna_admin.ui.staff

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swadratna.swadratna_admin.data.model.Activity
import com.swadratna.swadratna_admin.data.model.ActivityType
import com.swadratna.swadratna_admin.data.model.CreateStaffRequest
import com.swadratna.swadratna_admin.data.model.ShiftTiming
import com.swadratna.swadratna_admin.data.model.Staff
import com.swadratna.swadratna_admin.data.model.StaffStatus
import com.swadratna.swadratna_admin.data.model.UpdateStaffRequest
import com.swadratna.swadratna_admin.data.model.WorkingHours
import com.swadratna.swadratna_admin.data.repository.ActivityRepository
import com.swadratna.swadratna_admin.data.repository.StaffRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class StaffManagementViewModel @Inject constructor(
    private val staffRepository: StaffRepository,
    private val activityRepository: ActivityRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(StaffManagementUiState())
    val uiState: StateFlow<StaffManagementUiState> = _uiState.asStateFlow()
    
    private val _allStaff = mutableListOf<Staff>()
    
    fun loadStaff(storeId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            staffRepository.getStaff(storeId)
                .onSuccess { response ->
                    _allStaff.clear()
                    response.staff?.let { staffList ->
                        _allStaff.addAll(staffList)
                    }
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            staffList = _allStaff,
                            error = null
                        )
                    }
                    applyFiltersAndSort()
                }
                .onFailure { exception ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to load staff"
                        )
                    }
                }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFiltersAndSort()
    }

    fun createStaff(
        name: String,
        email: String,
        phone: String,
        address: String,
        role: String,
        salary: Double,
        joinDate: String,
        startTime: String,
        endTime: String,
        status: String,
        storeId: Int
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val request = CreateStaffRequest(
                address = address,
                email = email,
                joinDate = joinDate,
                name = name,
                phone = phone,
                role = role,
                salary = salary,
                shiftTiming = ShiftTiming(startTime, endTime),
                status = status,
                storeId = storeId
            )
            
            staffRepository.createStaff(request)
                .onSuccess { response ->
                    // Add activity tracking
                    activityRepository.addActivity(
                        ActivityType.STAFF_CREATED,
                        "New staff member added",
                        "Staff member '$name' has been successfully added with role '$role'"
                    )
                    
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = null
                        )
                    }
                    // Reload staff list to include the new staff member
                    loadStaff(storeId)
                }
                .onFailure { exception ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to create staff"
                        )
                    }
                }
        }
    }
    
    fun updateStaff(
        staffId: Int,
        name: String,
        email: String,
        phone: String,
        mobileNumber: String,
        address: String,
        role: String,
        salary: Double,
        joinDate: String,
        startTime: String,
        endTime: String,
        status: String,
        password: String? = null
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val request = UpdateStaffRequest(
                address = address,
                email = email,
                joinDate = joinDate,
                name = name,
                phone = phone,
                mobileNumber = mobileNumber,
                role = role,
                salary = salary,
                shiftTiming = ShiftTiming(startTime, endTime),
                status = status,
                password = password
            )
            
            staffRepository.updateStaff(staffId, request)
                .onSuccess { response ->
                    // Add activity tracking
                    activityRepository.addActivity(
                        ActivityType.STAFF_UPDATED,
                        "Staff member updated",
                        "Staff member '$name' has been successfully updated with role '$role'"
                    )
                    
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = null
                        )
                    }
                    // Reload staff list to reflect the updated staff member
                    val currentStoreId = _allStaff.firstOrNull()?.storeId
                    currentStoreId?.let { loadStaff(it) }
                }
                .onFailure { exception ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to update staff"
                        )
                    }
                }
        }
    }

    fun editStaff(staffId: Int) {
        // TODO: Implement edit staff functionality
    }

    fun deleteStaff(staffId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            // Find the staff member before deletion for activity tracking
            val staffToDelete = _allStaff.find { it.id == staffId }
            
            staffRepository.deleteStaff(staffId)
                .onSuccess {
                    // Add activity tracking
                    activityRepository.addActivity(
                        ActivityType.STAFF_DELETED,
                        "Staff member deleted",
                        "Staff member '${staffToDelete?.name ?: "Unknown"}' has been successfully deleted"
                    )
                    
                    _allStaff.removeIf { it.id == staffId }
                    applyFiltersAndSort()
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = null
                        )
                    }
                }
                .onFailure { exception ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to delete staff"
                        )
                    }
                }
        }
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
                staff.name?.lowercase()?.contains(searchQuery) == true || 
                staff.position?.lowercase()?.contains(searchQuery) == true
            
            // Apply status filter
            val matchesStatus = selectedFilter == null || staff.status.name == selectedFilter
            
            matchesSearch && matchesStatus
        }
        
        // Then sort the filtered list
        val sortedStaff = when (sortOrder) {
            "NAME_ASC" -> filteredStaff.sortedBy { it.name ?: "" }
            "NAME_DESC" -> filteredStaff.sortedByDescending { it.name ?: "" }
            "POSITION_ASC" -> filteredStaff.sortedBy { it.position ?: "" }
            "POSITION_DESC" -> filteredStaff.sortedByDescending { it.position ?: "" }
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
