package com.swadratna.swadratna_admin.ui.referral

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swadratna.swadratna_admin.data.model.Withdrawal
import com.swadratna.swadratna_admin.data.model.WithdrawalStatusUpdateRequest
import com.swadratna.swadratna_admin.data.repository.WithdrawalRepository
import com.swadratna.swadratna_admin.data.wrapper.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReferralUiState(
    val isLoading: Boolean = false,
    val withdrawals: List<Withdrawal> = emptyList(),
    val error: String? = null,
    val actionMessage: String? = null
)

@HiltViewModel
class ReferralViewModel @Inject constructor(
    private val repository: WithdrawalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReferralUiState())
    val uiState: StateFlow<ReferralUiState> = _uiState.asStateFlow()

    private var currentStatusFilter: String? = null

    init {
        fetchWithdrawals()
    }

    fun fetchWithdrawals(status: String? = null) {
        currentStatusFilter = status
        viewModelScope.launch {
            repository.getWithdrawals(status = status, limit = 50).collect { result ->
                when (result) {
                    is Result.Loading -> _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                    is Result.Success -> _uiState.value = _uiState.value.copy(isLoading = false, withdrawals = result.data.withdrawals ?: emptyList())
                    is Result.Error -> _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
                }
            }
        }
    }
    
    fun fetchPendingWithdrawals() {
        currentStatusFilter = "pending"
        viewModelScope.launch {
            repository.getPendingWithdrawals().collect { result ->
                 when (result) {
                    is Result.Loading -> _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                    is Result.Success -> _uiState.value = _uiState.value.copy(isLoading = false, withdrawals = result.data.withdrawals ?: emptyList())
                    is Result.Error -> _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
                }
            }
        }
    }

    fun approveWithdrawal(id: Long, transactionRef: String?, remarks: String?) {
        val request = WithdrawalStatusUpdateRequest(transactionRef = transactionRef, remarks = remarks)
        viewModelScope.launch {
            repository.updateWithdrawalStatus(id, "approve", request).collect { result ->
                handleActionResult(result, "Withdrawal approved successfully")
            }
        }
    }

    fun rejectWithdrawal(id: Long, reason: String?) {
        val request = WithdrawalStatusUpdateRequest(reason = reason)
        viewModelScope.launch {
            repository.updateWithdrawalStatus(id, "reject", request).collect { result ->
                handleActionResult(result, "Withdrawal rejected successfully")
            }
        }
    }

    fun processWithdrawal(id: Long, transactionRef: String?, remarks: String?) {
        val request = WithdrawalStatusUpdateRequest(transactionRef = transactionRef, remarks = remarks)
        viewModelScope.launch {
            repository.updateWithdrawalStatus(id, "process", request).collect { result ->
                handleActionResult(result, "Withdrawal processed successfully")
            }
        }
    }

    fun clearActionMessage() {
        _uiState.value = _uiState.value.copy(actionMessage = null)
    }

    private fun <T> handleActionResult(result: Result<T>, successMessage: String) {
        when (result) {
            is Result.Loading -> _uiState.value = _uiState.value.copy(isLoading = true, error = null, actionMessage = null)
            is Result.Success -> {
                _uiState.value = _uiState.value.copy(isLoading = false, actionMessage = successMessage)
                // Refresh list
                fetchWithdrawals(currentStatusFilter)
            }
            is Result.Error -> _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
        }
    }
}
