package com.swadratna.swadratna_admin.ui.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swadratna.swadratna_admin.data.remote.dto.CustomerDto
import com.swadratna.swadratna_admin.data.repository.CustomersRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserAccountUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val customers: List<CustomerDto> = emptyList(),
    val page: Int = 1,
    val limit: Int = 20,
    val hasNext: Boolean = false,
    val search: String = "",
    val status: String? = null
)

@HiltViewModel
class UserAccountViewModel @Inject constructor(
    private val repo: CustomersRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(UserAccountUiState())
    val uiState: StateFlow<UserAccountUiState> = _uiState.asStateFlow()

    fun load(reset: Boolean = false) {
        val current = _uiState.value
        val pageToLoad = if (reset) 1 else current.page
        _uiState.value = current.copy(isLoading = true, error = null, page = pageToLoad, customers = if (reset) emptyList() else current.customers)
        viewModelScope.launch {
            val result = repo.list(pageToLoad, current.limit, current.status, current.search.takeIf { it.isNotBlank() })
            result.fold(
                onSuccess = { resp ->
                    val newList = if (reset || pageToLoad == 1) resp.data else current.customers + resp.data
                    val hasNext = resp.pagination?.has_next == true
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        customers = newList,
                        hasNext = hasNext
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                }
            )
        }
    }

    fun search(query: String) {
        _uiState.value = _uiState.value.copy(search = query)
        load(reset = true)
    }

    fun loadNextPage() {
        if (!_uiState.value.hasNext || _uiState.value.isLoading) return
        _uiState.value = _uiState.value.copy(page = _uiState.value.page + 1)
        load(reset = false)
    }

    fun toggleBlock(customer: CustomerDto, block: Boolean) {
        // Optimistic update for immediate UI feedback
        val current = _uiState.value
        val original = current.customers.find { it.id == customer.id }
        val optimisticStatus = if (block) "blocked" else "active"
        val optimisticList = current.customers.map { if (it.id == customer.id) it.copy(blocked = block, status = optimisticStatus) else it }
        _uiState.value = current.copy(customers = optimisticList)

        viewModelScope.launch {
            val result = if (block) repo.block(customer.id) else repo.unblock(customer.id)
            result.fold(
                onSuccess = { updated ->
                    val finalBlocked = updated.blocked ?: block
                    val finalStatus = updated.status ?: optimisticStatus
                    val list = _uiState.value.customers.map { if (it.id == customer.id) it.copy(blocked = finalBlocked, status = finalStatus) else it }
                    _uiState.value = _uiState.value.copy(customers = list)
                },
                onFailure = { e ->
                    // Revert to original state on failure
                    val revertBlocked = original?.blocked ?: (original?.status == "blocked")
                    val revertStatus = original?.status
                    val list = _uiState.value.customers.map { if (it.id == customer.id) it.copy(blocked = revertBlocked, status = revertStatus) else it }
                    _uiState.value = _uiState.value.copy(error = e.message, customers = list)
                }
            )
        }
    }

    fun delete(customer: CustomerDto) {
        viewModelScope.launch {
            val result = repo.delete(customer.id)
            result.fold(
                onSuccess = {
                    val list = _uiState.value.customers.map { if (it.id == customer.id) it.copy(deleted = true) else it }
                    _uiState.value = _uiState.value.copy(customers = list)
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(error = e.message)
                }
            )
        }
    }
}
