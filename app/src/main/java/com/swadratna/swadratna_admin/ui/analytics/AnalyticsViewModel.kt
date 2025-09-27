package com.swadratna.swadratna_admin.ui.analytics

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swadratna.swadratna_admin.data.model.Analytics
import com.swadratna.swadratna_admin.data.repository.AnalyticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val repo: AnalyticsRepository
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
        _state.update { it.copy(franchiseFilter = name) }
        refresh()
    }

    private fun loadFranchises() {
        val franchises = listOf("All", "Franchise A", "Franchise B", "Franchise C")
        _state.update { it.copy(availableFranchises = franchises) }
    }

    fun refresh() = viewModelScope.launch {
        _state.update { it.copy(loading = true, error = null) }
        try {
            val data = repo.loadDashboard(
                franchise = _state.value.franchiseFilter,
                from = null, to = null
            )
            _state.update { it.copy(loading = false, analytics = data) }
        } catch (t: Throwable) {
            _state.update { it.copy(loading = false, error = t.message) }
        }
    }

//    fun exportReport(context: Context) {
//        viewModelScope.launch {
//            state.value.analytics?.let { data ->
//                AnalyticsPdfExporter.exportAnalyticsPdf(context, data)
//            }
//        }
//    }
}
