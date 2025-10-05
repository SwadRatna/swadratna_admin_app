package com.swadratna.swadratna_admin.ui.menu

import com.swadratna.swadratna_admin.data.model.MenuItem

sealed interface MenuUiState {
    data object Loading : MenuUiState
    data class Success(val items: List<MenuItem>, val successMessage: String? = null) : MenuUiState
    data class Error(val message: String) : MenuUiState
}
