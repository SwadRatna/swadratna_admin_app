package com.swadratna.swadratna_admin.ui.sales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swadratna.swadratna_admin.data.model.SalesResponse
import com.swadratna.swadratna_admin.data.model.Store
import com.swadratna.swadratna_admin.data.repository.SalesRepository
import com.swadratna.swadratna_admin.data.repository.StoreRepository
import com.swadratna.swadratna_admin.data.wrapper.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SalesUiState(
    val isLoading: Boolean = false,
    val salesResponse: SalesResponse? = null,
    val stores: List<Store> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class SalesViewModel @Inject constructor(
    private val repository: SalesRepository,
    private val storeRepository: StoreRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SalesUiState())
    val uiState: StateFlow<SalesUiState> = _uiState.asStateFlow()

    init {
        fetchStores()
    }

    private fun fetchStores() {
        viewModelScope.launch {
            try {
                val result = storeRepository.getStores(page = 1, limit = 100, restaurantId = 1000001)
                result.onSuccess { response ->
                    _uiState.value = _uiState.value.copy(stores = response.stores)
                }
            } catch (e: Exception) {
                // Handle error silently or log it
            }
        }
    }

    fun fetchSales(
        date: String? = null,
        fromDate: String? = null,
        toDate: String? = null,
        locationIds: String? = "1000003"
    ) {
        viewModelScope.launch {
            repository.getSales(date, fromDate, toDate, locationIds).collect { result ->
                when (result) {
                    is Result.Loading -> _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                    is Result.Success -> _uiState.value = _uiState.value.copy(isLoading = false, salesResponse = result.data)
                    is Result.Error -> _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
                }
            }
        }
    }
}
