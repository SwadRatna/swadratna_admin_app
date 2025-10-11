package com.swadratna.swadratna_admin.ui.store

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swadratna.swadratna_admin.data.model.Activity
import com.swadratna.swadratna_admin.data.model.ActivityType
import com.swadratna.swadratna_admin.data.model.Store
import com.swadratna.swadratna_admin.data.model.StoreAddressRequest
import com.swadratna.swadratna_admin.data.model.StoreRequest
import com.swadratna.swadratna_admin.data.repository.ActivityRepository
import com.swadratna.swadratna_admin.data.repository.StoreRepository
import com.swadratna.swadratna_admin.utils.SharedPrefsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StoreViewModel @Inject constructor(
    private val storeRepository: StoreRepository,
    private val activityRepository: ActivityRepository,
    private val sharedPrefsManager: SharedPrefsManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(StoreUiState())
    val uiState: StateFlow<StoreUiState> = _uiState.asStateFlow()

    init {
        loadStores()
    }
    
    private fun loadStores() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        
        viewModelScope.launch {
            try {
                val result = storeRepository.getStores(page = 1, limit = 20, restaurantId = 1000001)
                result.onSuccess { response ->
                    _uiState.value = _uiState.value.copy(
                        stores = response.stores,
                        isLoading = false
                    )
                    updateFilteredStores()
                }.onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to load stores"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "An unexpected error occurred"
                )
            }
        }
    }

    fun onEvent(event: StoreEvent) {
        when (event) {
            is StoreEvent.SearchQueryChanged -> {
                _uiState.value = _uiState.value.copy(searchQuery = event.query)
                updateFilteredStores()
            }
            is StoreEvent.FilterByStatus -> {
                _uiState.value = _uiState.value.copy(filterStatus = event.status)
                updateFilteredStores()
            }
            is StoreEvent.SortBy -> {
                _uiState.value = _uiState.value.copy(sortOption = event.option)
                updateFilteredStores()
            }
            is StoreEvent.ToggleFilterMenu -> {
                _uiState.value = _uiState.value.copy(
                    isFilterMenuVisible = !_uiState.value.isFilterMenuVisible,
                    isSortMenuVisible = false
                )
            }
            is StoreEvent.ToggleSortMenu -> {
                _uiState.value = _uiState.value.copy(
                    isSortMenuVisible = !_uiState.value.isSortMenuVisible,
                    isFilterMenuVisible = false
                )
            }
            is StoreEvent.RefreshStores -> {
                loadStores()
            }
            is StoreEvent.ResetEditMode -> {
                _uiState.value = _uiState.value.copy(
                    storeToEdit = null,
                    isEditMode = false
                )
            }
            is StoreEvent.EditStore -> {
                val storeToEdit = _uiState.value.stores.find { it.id == event.storeId }
                if (storeToEdit != null) {
                    _uiState.value = _uiState.value.copy(
                        storeToEdit = storeToEdit,
                        isEditMode = true
                    )
                }
            }
            is StoreEvent.CreateStore -> {
                createStore(event)
            }
            is StoreEvent.UpdateStore -> {
                updateStore(event)
            }
            is StoreEvent.DeleteStore -> {
                deleteStore(event.storeId)
            }
        }
    }
    
    private fun updateFilteredStores() {
        val filteredList = _uiState.value.stores.filter { store ->
            val matchesSearch = _uiState.value.searchQuery.isEmpty() ||
                    store.name.contains(_uiState.value.searchQuery, ignoreCase = true) ||
                    store.getFullAddress().contains(_uiState.value.searchQuery, ignoreCase = true)
            
            val matchesStatus = _uiState.value.filterStatus == null ||
                    store.status == _uiState.value.filterStatus
            
            matchesSearch && matchesStatus
        }
        
        val sortedList = when (_uiState.value.sortOption) {
            "NAME_ASC" -> filteredList.sortedBy { it.name }
            "NAME_DESC" -> filteredList.sortedByDescending { it.name }
            "DATE_ASC" -> filteredList.sortedBy { it.createdAt }
            "DATE_DESC" -> filteredList.sortedByDescending { it.createdAt }
            else -> filteredList
        }
        
        _uiState.value = _uiState.value.copy(filteredStores = sortedList)
    }
    
    private fun createStore(event: StoreEvent.CreateStore) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        
        viewModelScope.launch {
            try {
                val storeRequest = StoreRequest(
                    address = StoreAddressRequest(
                        plotNo = event.plotNo,
                        poBoxNo = event.poBoxNo,
                        street1 = event.street1,
                        street2 = event.street2,
                        locality = event.locality,
                        city = event.city,
                        pincode = event.pincode,
                        landmark = event.landmark
                    ),
                    status = "active",
                    locationMobileNumber = event.locationMobileNumber,
                    restaurantId = 1000001,
                    numberOfTables = event.numberOfTables
                )
                
                val result = storeRepository.createStore(storeRequest)
                result.onSuccess { newStore ->
                    // Add activity tracking
                    activityRepository.addActivity(
                        ActivityType.STORE_CREATED,
                        "New store created",
                        "Store '${newStore.name}' has been successfully created at ${newStore.getFullAddress()}"
                    )
                    
                    // Refresh the store list to include the new store
                    loadStores()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = null
                    )
                }.onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to create store"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "An unexpected error occurred"
                )
            }
        }
    }
    
    private fun updateStore(event: StoreEvent.UpdateStore) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        
        viewModelScope.launch {
            try {
                val storeRequest = StoreRequest(
                    address = StoreAddressRequest(
                        plotNo = event.plotNo,
                        poBoxNo = event.poBoxNo,
                        street1 = event.street1,
                        street2 = event.street2,
                        locality = event.locality,
                        city = event.city,
                        pincode = event.pincode,
                        landmark = event.landmark
                    ),
                    status = "active",
                    locationMobileNumber = event.locationMobileNumber,
                    restaurantId = 1000001,
                    numberOfTables = event.numberOfTables
                )
                
                val result = storeRepository.updateStore(event.storeId, storeRequest)
                result.onSuccess { updatedStore ->
                    // Add activity tracking
                    activityRepository.addActivity(
                        ActivityType.STORE_UPDATED,
                        "Store updated",
                        "Store '${updatedStore.name}' has been successfully updated"
                    )
                    
                    // Refresh the store list to reflect the updated store
                    loadStores()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = null,
                        storeToEdit = null,
                        isEditMode = false
                    )
                }.onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to update store"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "An unexpected error occurred"
                )
            }
        }
    }
    
    private fun deleteStore(storeId: Int) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                // Find the store name before deletion for activity tracking
                val storeToDelete = _uiState.value.stores.find { it.id == storeId }
                
                storeRepository.deleteStore(storeId).onSuccess {
                    // Add activity tracking
                    activityRepository.addActivity(
                        ActivityType.STORE_DELETED,
                        "Store deleted",
                        "Store '${storeToDelete?.name ?: "Unknown"}' has been successfully deleted"
                    )
                    
                    // Remove the store from the current list
                    val updatedStores = _uiState.value.stores.filter { it.id != storeId }
                    _uiState.value = _uiState.value.copy(
                        stores = updatedStores,
                        isLoading = false
                    )
                    updateFilteredStores()
                }.onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to delete store"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "An unexpected error occurred"
                )
            }
        }
    }
    

}

data class StoreUiState(
    val searchQuery: String = "",
    val stores: List<Store> = emptyList(),
    val filteredStores: List<Store> = emptyList(),
    val filterStatus: String? = null,
    val sortOption: String = "NAME_ASC",
    val isFilterMenuVisible: Boolean = false,
    val isSortMenuVisible: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val storeToEdit: Store? = null,
    val isEditMode: Boolean = false,
    val currentPage: Int = 1,
    val totalPages: Int = 1
)

sealed interface StoreEvent {
    data class SearchQueryChanged(val query: String) : StoreEvent
    data class FilterByStatus(val status: String?) : StoreEvent
    data class SortBy(val option: String) : StoreEvent
    object ToggleFilterMenu : StoreEvent
    object ToggleSortMenu : StoreEvent
    object RefreshStores : StoreEvent
    object ResetEditMode : StoreEvent
    data class EditStore(val storeId: Int) : StoreEvent
    data class CreateStore(
        val plotNo: String,
        val poBoxNo: String,
        val street1: String,
        val street2: String,
        val locality: String,
        val city: String,
        val pincode: String,
        val landmark: String,
        val locationMobileNumber: String,
        val numberOfTables: Int
    ) : StoreEvent
    data class UpdateStore(
        val storeId: Int,
        val plotNo: String,
        val poBoxNo: String,
        val street1: String,
        val street2: String,
        val locality: String,
        val city: String,
        val pincode: String,
        val landmark: String,
        val locationMobileNumber: String,
        val numberOfTables: Int
    ) : StoreEvent
    data class DeleteStore(val storeId: Int) : StoreEvent
}