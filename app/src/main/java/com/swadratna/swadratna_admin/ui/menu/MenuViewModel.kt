package com.swadratna.swadratna_admin.ui.menu

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swadratna.swadratna_admin.data.model.MenuCategory
import com.swadratna.swadratna_admin.data.model.MenuItem
import com.swadratna.swadratna_admin.data.remote.toDomain
import com.swadratna.swadratna_admin.data.repository.MenuRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class MenuViewModel @Inject constructor(
    private val repository: MenuRepository
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow<MenuCategory?>(null)
    val selectedCategory: StateFlow<MenuCategory?> = _selectedCategory.asStateFlow()

    private val _uiState = MutableStateFlow<MenuUiState>(MenuUiState.Loading)
    val uiState: StateFlow<MenuUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun selectCategory(category: MenuCategory?) {
        _selectedCategory.value = category
        load()
    }

    fun toggleAvailability(item: MenuItem) {
        viewModelScope.launch {
            val itemId = item.id ?: return@launch
            val request = com.swadratna.swadratna_admin.data.model.ToggleAvailabilityRequest(!item.isAvailable)
            
            repository.toggleMenuItemAvailability(itemId, request)
                .onSuccess { response ->
                    // Treat HTTP 200 responses with success messages as successful
                    val isSuccessful = response.success || response.message.contains("successfully", ignoreCase = true)
                    if (isSuccessful) {
                        // Update the local state immediately for better UX
                        val current = (_uiState.value as? MenuUiState.Success)?.items ?: return@onSuccess
                        val updated = current.map { 
                            if (it.id == item.id) it.copy(isAvailable = !it.isAvailable) else it 
                        }
                        val statusText = if (!item.isAvailable) "enabled" else "disabled"
                        _uiState.value = MenuUiState.Success(
                            items = updated,
                            successMessage = "Menu item availability ${statusText} successfully"
                        )
                    } else {
                        _uiState.value = MenuUiState.Error(
                            response.message ?: "Failed to toggle availability"
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.value = MenuUiState.Error(
                        error.message ?: "Failed to toggle availability"
                    )
                }
        }
    }

    fun clearSuccessMessage() {
        val currentState = _uiState.value
        if (currentState is MenuUiState.Success) {
            _uiState.value = currentState.copy(successMessage = null)
        }
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.value = MenuUiState.Loading
            val categoryId = _selectedCategory.value?.id
            repository.getMenuItems(categoryId = categoryId)
                .onSuccess { response ->
                    val items = response.items?.map { it.toDomain() } ?: emptyList()
                    _uiState.value = MenuUiState.Success(items = items, successMessage = null)
                }
                .onFailure { e ->
                    _uiState.value = MenuUiState.Error(e.message ?: "Unknown error")
                }
        }
    }
}

