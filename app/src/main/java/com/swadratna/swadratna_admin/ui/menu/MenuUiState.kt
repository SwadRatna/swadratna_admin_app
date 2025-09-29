package com.swadratna.swadratna_admin.ui.menu

import com.swadratna.swadratna_admin.data.model.MenuItem

sealed interface MenuUiState {
    data object Loading : MenuUiState
    data class Success(val items: List<MenuItem>) : MenuUiState
    data class Error(val message: String) : MenuUiState
}
