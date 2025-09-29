package com.swadratna.swadratna_admin.ui.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swadratna.swadratna_admin.data.model.MenuCategory
import com.swadratna.swadratna_admin.data.model.MenuItem
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

    private val _selectedCategory = MutableStateFlow(MenuCategory.ALL)
    val selectedCategory: StateFlow<MenuCategory> = _selectedCategory.asStateFlow()

    private val _uiState = MutableStateFlow<MenuUiState>(MenuUiState.Loading)
    val uiState: StateFlow<MenuUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun selectCategory(category: MenuCategory) {
        _selectedCategory.value = category
        load()
    }

    fun toggleAvailability(item: MenuItem) {
        val current = (_uiState.value as? MenuUiState.Success)?.items ?: return
        val updated = current.map { if (it.id == item.id) it.copy(isAvailable = !it.isAvailable) else it }
        _uiState.value = MenuUiState.Success(updated)
        // TODO: Optionally call repository to persist availability change (PATCH)
    }

    fun setUseMock(enabled: Boolean) {
        repository.useMock = enabled
        load()
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.value = MenuUiState.Loading
            runCatching {
                repository.getMenu(_selectedCategory.value)
            }.onSuccess { list ->
                _uiState.value = MenuUiState.Success(list)
            }.onFailure { e ->
                _uiState.value = MenuUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

