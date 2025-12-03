package com.swadratna.swadratna_admin.ui.staff

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swadratna.swadratna_admin.data.model.Staff
import com.swadratna.swadratna_admin.data.repository.StaffRepository
import com.swadratna.swadratna_admin.data.repository.StoreRepository
import com.swadratna.swadratna_admin.utils.ApiConstants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AllStaffViewModel @Inject constructor(
    private val staffRepository: StaffRepository,
    private val storeRepository: StoreRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AllStaffUiState())
    val uiState: StateFlow<AllStaffUiState> = _uiState.asStateFlow()
    
    private val _allStaff = mutableListOf<Staff>()
    
    init {
        loadAllStaff()
    }
    
    fun loadAllStaff() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val allStaff = mutableListOf<Staff>()
                
                // First, get all staff using the getAllStaff() method which should include "General" staff
                staffRepository.getAllStaff().onSuccess { staffResponse ->
                    staffResponse.staff?.let { generalStaff ->
                        allStaff.addAll(generalStaff)
                    }
                }.onFailure { error ->
                    println("Error fetching all staff: ${error.message}")
                }
                
                // Also fetch staff from specific stores to ensure we have complete coverage
                val storesResult = storeRepository.getStores(page = 1, limit = 1000, restaurantId = ApiConstants.RESTAURANT_ID)
                
                storesResult.onSuccess { storeResponse ->
                    // Fetch staff for each store
                    storeResponse.stores.forEach { store ->
                        staffRepository.getStaff(store.id).onSuccess { staffResponse ->
                            staffResponse.staff?.forEach { staff ->
                                allStaff.add(staff)
                            }
                        }.onFailure { error ->
                            // Log error but continue with other stores
                            println("Error fetching staff for store ${store.id}: ${error.message}")
                        }
                    }
                    
                    // Remove duplicates based on staff ID
                    val uniqueStaff = allStaff.distinctBy { it.id }
                    _allStaff.clear()
                    _allStaff.addAll(uniqueStaff)
                    
                    applyFiltersAndSort()
                    
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = null
                        )
                    }
                }.onFailure { error ->
                    // Even if stores fail, we might still have general staff
                    val uniqueStaff = allStaff.distinctBy { it.id }
                    _allStaff.clear()
                    _allStaff.addAll(uniqueStaff)
                    
                    applyFiltersAndSort()
                    
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to load stores"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "An unexpected error occurred"
                    )
                }
            }
        }
    }
    
    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
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
    
    fun onEvent(event: AllStaffEvent) {
        when (event) {
            is AllStaffEvent.ToggleFilterMenu -> {
                _uiState.value = _uiState.value.copy(
                    isFilterMenuVisible = !_uiState.value.isFilterMenuVisible,
                    isSortMenuVisible = false
                )
            }
            is AllStaffEvent.ToggleSortMenu -> {
                _uiState.value = _uiState.value.copy(
                    isSortMenuVisible = !_uiState.value.isSortMenuVisible,
                    isFilterMenuVisible = false
                )
            }
        }
    }
    
    private fun applyFiltersAndSort() {
        val searchQuery = _uiState.value.searchQuery.lowercase()
        val selectedFilter = _uiState.value.selectedFilter
        val sortOrder = _uiState.value.selectedSortOrder
        
        // Apply filters
        val filteredStaff = _allStaff.filter { staff ->
            // Apply search filter
            val matchesSearch = searchQuery.isEmpty() || 
                staff.name?.lowercase()?.contains(searchQuery) == true || 
                staff.position?.lowercase()?.contains(searchQuery) == true
            
            // Apply status filter
            val matchesStatus = selectedFilter == null || staff.status.name == selectedFilter
            
            matchesSearch && matchesStatus
        }
        
        // Sort the filtered list
        val sortedStaff = when (sortOrder) {
            "NAME_ASC" -> filteredStaff.sortedBy { it.name ?: "" }
            "NAME_DESC" -> filteredStaff.sortedByDescending { it.name ?: "" }
            "POSITION_ASC" -> filteredStaff.sortedBy { it.position ?: "" }
            "POSITION_DESC" -> filteredStaff.sortedByDescending { it.position ?: "" }
            else -> filteredStaff
        }
        
        _uiState.update { it.copy(staffList = sortedStaff) }
    }
}

data class AllStaffUiState(
    val searchQuery: String = "",
    val staffList: List<Staff> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isFilterMenuVisible: Boolean = false,
    val isSortMenuVisible: Boolean = false,
    val selectedFilter: String? = null,
    val selectedSortOrder: String = "NAME_ASC"
)

sealed interface AllStaffEvent {
    object ToggleFilterMenu : AllStaffEvent
    object ToggleSortMenu : AllStaffEvent
}