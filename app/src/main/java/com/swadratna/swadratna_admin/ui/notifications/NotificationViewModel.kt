package com.swadratna.swadratna_admin.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swadratna.swadratna_admin.data.model.Activity
import com.swadratna.swadratna_admin.data.repository.ActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val activityRepository: ActivityRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()
    
    init {
        loadActivities()
    }
    
    private fun loadActivities() {
        viewModelScope.launch {
            activityRepository.getAllActivities().collect { activities ->
                _uiState.value = _uiState.value.copy(
                    activities = activities,
                    isLoading = false
                )
            }
        }
    }
    
    fun clearAllActivities() {
        viewModelScope.launch {
            activityRepository.clearAllActivities()
        }
    }
}

data class NotificationUiState(
    val activities: List<Activity> = emptyList(),
    val isLoading: Boolean = true
)