package com.swadratna.swadratna_admin.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swadratna.swadratna_admin.data.model.AnalyticsData
import com.swadratna.swadratna_admin.data.repository.AnalyticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val analyticsRepository: AnalyticsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AnalyticsUiState>(AnalyticsUiState.Loading)
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    private val _selectedFranchise = MutableStateFlow<String?>(null)
    val selectedFranchise: StateFlow<String?> = _selectedFranchise.asStateFlow()

    init {
        fetchAnalyticsData()
    }

    fun fetchAnalyticsData() {
        viewModelScope.launch {
            _uiState.value = AnalyticsUiState.Loading
            analyticsRepository.getAnalyticsData().collect { result ->
                result.onSuccess { data ->
                    _uiState.value = AnalyticsUiState.Success(data)
                }.onFailure { error ->
                    _uiState.value = AnalyticsUiState.Error(error.message ?: "Unknown error")
                }
            }
        }
    }

    fun selectFranchise(franchiseId: String?) {
        _selectedFranchise.value = franchiseId
    }
}

sealed class AnalyticsUiState {
    object Loading : AnalyticsUiState()
    data class Success(val data: AnalyticsData) : AnalyticsUiState()
    data class Error(val message: String) : AnalyticsUiState()
}