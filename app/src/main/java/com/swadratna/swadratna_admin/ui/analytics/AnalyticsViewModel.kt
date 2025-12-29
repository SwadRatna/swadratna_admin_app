package com.swadratna.swadratna_admin.ui.analytics

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swadratna.swadratna_admin.data.model.Analytics
import com.swadratna.swadratna_admin.data.repository.AnalyticsRepository
import com.swadratna.swadratna_admin.data.repository.StoreRepository
import com.swadratna.swadratna_admin.utils.ApiConstants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.swadratna.swadratna_admin.data.model.SalesInfoItem

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val repo: AnalyticsRepository,
    private val storeRepository: StoreRepository
) : ViewModel() {

    data class UiState(
        val loading: Boolean = true,
        val analytics: Analytics? = null,
        val salesInfo: List<SalesInfoItem> = emptyList(),
        val error: String? = null,
        val timeframe: String = "YTD",
        val franchiseFilter: String? = null,
        val availableFranchises: List<String> = emptyList()
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    init {
        refresh()
        loadFranchises()
    }

    private fun loadFranchises() {
        viewModelScope.launch {
            runCatching {
                storeRepository.getStores(page = 1, limit = 100, restaurantId = ApiConstants.RESTAURANT_ID)
            }.onSuccess { result ->
                result.onSuccess { response ->
                    val names = response.stores.map { it.name }
                    val franchises = listOf("All") + names
                    _state.update { it.copy(availableFranchises = franchises) }
                }.onFailure { e ->
                    _state.update { it.copy(availableFranchises = listOf("All")) }
                }
            }.onFailure {
                _state.update { it.copy(availableFranchises = listOf("All")) }
            }
        }
    }

    fun refresh() = viewModelScope.launch {
        _state.update { it.copy(loading = true, error = null) }
        
        // Parallel execution could be better, but sequential is fine for now
        val dashboardResult = runCatching {
            repo.loadDashboard(franchise = _state.value.franchiseFilter, from = null, to = null)
        }
        
        val salesResult = runCatching {
            // Using the date provided in the example for now
            repo.getSalesInfo("25-12-2025")
        }

        if (dashboardResult.isSuccess) {
            _state.update { 
                it.copy(
                    loading = false, 
                    analytics = dashboardResult.getOrNull(),
                    salesInfo = salesResult.getOrDefault(emptyList())
                ) 
            }
        } else {
            _state.update { it.copy(loading = false, error = dashboardResult.exceptionOrNull()?.message ?: "Unknown error") }
        }
    }
}
