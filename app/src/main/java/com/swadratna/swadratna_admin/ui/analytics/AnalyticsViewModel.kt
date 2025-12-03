package com.swadratna.swadratna_admin.ui.analytics

import android.content.Context
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

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val repo: AnalyticsRepository,
    private val storeRepository: StoreRepository
) : ViewModel() {

    data class UiState(
        val loading: Boolean = true,
        val analytics: Analytics? = null,
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

    fun setFranchiseFilter(name: String?) {
        val normalized = name?.takeIf { it.isNotBlank() && it.lowercase() != "all" }
        _state.update { it.copy(franchiseFilter = normalized) }
        refresh()
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
                    // Fallback to an empty list (plus All) on failure
                    _state.update { it.copy(availableFranchises = listOf("All")) }
                }
            }.onFailure {
                _state.update { it.copy(availableFranchises = listOf("All")) }
            }
        }
    }

    fun refresh() = viewModelScope.launch {
        _state.update { it.copy(loading = true, error = null) }
        runCatching {
            repo.loadDashboard(franchise = _state.value.franchiseFilter, from = null, to = null)
        }.onSuccess { data ->
            _state.update { it.copy(loading = false, analytics = data) }
        }.onFailure { e ->
            _state.update { it.copy(loading = false, error = e.message ?: "Unknown error") }
        }
    }
}
