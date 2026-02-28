package com.swadratna.swadratna_admin.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swadratna.swadratna_admin.data.model.MenuItem
import com.swadratna.swadratna_admin.data.repository.MenuRepository
import com.swadratna.swadratna_admin.data.remote.toDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class StoreLocationMenuUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val menu: List<MenuItem> = emptyList(),
    val search: String = "",
    val locationId: String = "",
    val editingItem: MenuItem? = null
)

@HiltViewModel
class StoreLocationMenuViewModel @Inject constructor(
    private val repo: MenuRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(StoreLocationMenuUiState())
    val uiState: StateFlow<StoreLocationMenuUiState> = _uiState.asStateFlow()

    fun setLocation(id: String) {
        _uiState.value = _uiState.value.copy(locationId = id)
    }

    fun setSearch(q: String) {
        _uiState.value = _uiState.value.copy(search = q)
        load()
    }

    fun load() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, error = null)
            val all = mutableListOf<MenuItem>()
            var page = 1
            val limit = 200
            while (true) {
                val result = repo.getMenuItems(
                    categoryId = null,
                    isAvailable = null,
                    search = state.search.takeIf { it.isNotBlank() },
                    page = page,
                    limit = limit
                )
                var continueLoading = false
                result.onSuccess { resp ->
                    val items = resp.items?.map { it.toDomain() } ?: emptyList()
                    all.addAll(items)
                    val p = resp.pagination
                    continueLoading = when {
                        p?.hasNext == true -> true
                        p?.totalPages != null && p.page != null -> (p.page ?: 1) < (p.totalPages ?: 1)
                        else -> items.size >= limit
                    }
                }.onFailure { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                    return@launch
                }
                if (!continueLoading) break
                page += 1
            }
            _uiState.value = _uiState.value.copy(isLoading = false, menu = all)
        }
    }

    fun openEdit(item: MenuItem) {
        _uiState.value = _uiState.value.copy(editingItem = item)
    }

    fun dismissEdit() {
        _uiState.value = _uiState.value.copy(editingItem = null)
    }

    fun saveEdit(locationPrice: Double?, available: Boolean, reason: String?) {
        val state = _uiState.value
        val item = state.editingItem ?: return
        val locId = state.locationId.toLongOrNull() ?: return
        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true, error = null)
            repo.updateMenuItemAtLocation(
                locationId = locId,
                itemId = item.id ?: return@launch,
                locationPrice = locationPrice,
                isAvailable = available,
                unavailableReason = reason
            ).onSuccess {
                _uiState.value = _uiState.value.copy(isSaving = false, editingItem = null)
                load()
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(isSaving = false, error = e.message)
            }
        }
    }
}
