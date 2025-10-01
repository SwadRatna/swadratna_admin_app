package com.swadratna.swadratna_admin.ui.store

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swadratna.swadratna_admin.data.model.Store
import com.swadratna.swadratna_admin.data.model.StoreStatus
import com.swadratna.swadratna_admin.utils.SharedPrefsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class StoreViewModel @Inject constructor(
    private val sharedPrefsManager: SharedPrefsManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(StoreUiState())
    val uiState: StateFlow<StoreUiState> = _uiState.asStateFlow()

    init {
        val savedStores = sharedPrefsManager.getStores()
        
        val initialStores = if (savedStores.isNotEmpty()) {
            savedStores
        } else {
            listOf(
                Store(
                    id = "1",
                    name = "Coastal Grill",
                    location = "Mumbai, Maharashtra",
                    address = "123 Coastal Avenue, Bandra West",
                    creationDate = LocalDate.of(2023, 5, 15),
                    status = StoreStatus.ACTIVE,
                    imageUrl = null
                ),
                Store(
                    id = "2",
                    name = "Urban Eatery",
                    location = "Delhi, NCR",
                    address = "45 Connaught Place, New Delhi",
                    creationDate = LocalDate.of(2023, 8, 22),
                    status = StoreStatus.ACTIVE,
                    imageUrl = null
                ),
                Store(
                    id = "3",
                    name = "Parkside Bistro",
                    location = "Bangalore, Karnataka",
                    address = "78 MG Road, Indiranagar",
                    creationDate = LocalDate.of(2024, 1, 10),
                    status = StoreStatus.PENDING,
                    imageUrl = null
                )
            )
        }
        
        _uiState.value = StoreUiState(
            searchQuery = "",
            stores = initialStores
        )
        
        updateFilteredStores()
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
                updateFilteredStores()
            }
            is StoreEvent.ResetEditMode -> {
                _uiState.value = _uiState.value.copy(
                    storeToEdit = null,
                    isEditMode = false
                )
            }
            is StoreEvent.CreateStore -> {
                val newStore = Store(
                    id = UUID.randomUUID().toString(),
                    name = event.name,
                    location = event.location,
                    address = event.address,
                    creationDate = LocalDate.now(),
                    status = event.status,
                    imageUrl = null
                )
                
                val updatedStores = _uiState.value.stores.toMutableList().apply {
                    add(0, newStore)
                }
                
                _uiState.value = _uiState.value.copy(stores = updatedStores)
                
                viewModelScope.launch {
                    sharedPrefsManager.saveStores(updatedStores)
                }
                
                updateFilteredStores()
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
            is StoreEvent.DeleteStore -> {
                val updatedStores = _uiState.value.stores.filter { it.id != event.storeId }
                
                _uiState.value = _uiState.value.copy(stores = updatedStores)
                
                viewModelScope.launch {
                    sharedPrefsManager.saveStores(updatedStores)
                }
                
                updateFilteredStores()
            }
            is StoreEvent.UpdateStore -> {
                val updatedStores = _uiState.value.stores.map { store ->
                    if (store.id == event.id) {
                        store.copy(
                            name = event.name,
                            location = event.location,
                            address = event.address,
                            status = event.status
                        )
                    } else {
                        store
                    }
                }
                
                _uiState.value = _uiState.value.copy(
                    stores = updatedStores,
                    storeToEdit = null,
                    isEditMode = false
                )
                
                viewModelScope.launch {
                    sharedPrefsManager.saveStores(updatedStores)
                }
                
                updateFilteredStores()
            }
        }
    }
    
    private fun updateFilteredStores() {
        val filteredList = _uiState.value.stores.filter { store ->
            val matchesSearch = _uiState.value.searchQuery.isEmpty() ||
                    store.name.contains(_uiState.value.searchQuery, ignoreCase = true) ||
                    store.location.contains(_uiState.value.searchQuery, ignoreCase = true)
            
            val matchesStatus = _uiState.value.filterStatus == null ||
                    store.status.name == _uiState.value.filterStatus
            
            matchesSearch && matchesStatus
        }
        
        val sortedList = when (_uiState.value.sortOption) {
            "NAME_ASC" -> filteredList.sortedBy { it.name }
            "NAME_DESC" -> filteredList.sortedByDescending { it.name }
            "DATE_ASC" -> filteredList.sortedBy { it.creationDate }
            "DATE_DESC" -> filteredList.sortedByDescending { it.creationDate }
            else -> filteredList
        }
        
        _uiState.value = _uiState.value.copy(filteredStores = sortedList)
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
    val isEditMode: Boolean = false
)

sealed interface StoreEvent {
    data class SearchQueryChanged(val query: String) : StoreEvent
    data class FilterByStatus(val status: String?) : StoreEvent
    data class SortBy(val option: String) : StoreEvent
    object ToggleFilterMenu : StoreEvent
    object ToggleSortMenu : StoreEvent
    object RefreshStores : StoreEvent
    object ResetEditMode : StoreEvent
    data class CreateStore(
        val name: String,
        val location: String,
        val address: String,
        val status: StoreStatus
    ) : StoreEvent
    data class EditStore(val storeId: String) : StoreEvent
    data class DeleteStore(val storeId: String) : StoreEvent
    data class UpdateStore(
        val id: String,
        val name: String,
        val location: String,
        val address: String,
        val status: StoreStatus
    ) : StoreEvent
}