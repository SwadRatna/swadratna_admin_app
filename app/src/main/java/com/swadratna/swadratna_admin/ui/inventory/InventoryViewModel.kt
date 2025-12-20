package com.swadratna.swadratna_admin.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swadratna.swadratna_admin.data.model.CreateIngredientRequest
import com.swadratna.swadratna_admin.data.model.Ingredient
import com.swadratna.swadratna_admin.data.model.StockInRequest
import com.swadratna.swadratna_admin.data.model.StockOutRequest
import com.swadratna.swadratna_admin.data.model.WastageRequest
import com.swadratna.swadratna_admin.data.model.AdjustmentRequest
import com.swadratna.swadratna_admin.data.repository.InventoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class InventoryUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val ingredients: List<Ingredient> = emptyList(),
    val lowStock: List<Ingredient> = emptyList(),
    val shouldPromptLowStock: Boolean = false
)

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val repository: InventoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InventoryUiState())
    val uiState: StateFlow<InventoryUiState> = _uiState

    private var currentStoreId: Int = 0

    fun init(storeId: Int) {
        currentStoreId = storeId
        loadIngredients()
        loadLowStock()
    }

    fun loadIngredients() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val result = repository.getIngredients(null)
            _uiState.value = result.fold(
                onSuccess = { _uiState.value.copy(isLoading = false, ingredients = it, error = null) },
                onFailure = { _uiState.value.copy(isLoading = false, error = it.message) }
            )
        }
    }

    fun createIngredient(
        name: String,
        category: String,
        unit: String,
        reorderLevel: Int,
        costPerUnit: Double,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val req = CreateIngredientRequest(
                name = name,
                category = category,
                unit = unit,
                reorderLevel = reorderLevel,
                costPerUnit = costPerUnit
            )
            val result = repository.createIngredient(req, currentStoreId)
            result.fold(
                onSuccess = {
                    onResult(true, it.message)
                    loadIngredients()
                },
                onFailure = { onResult(false, it.message ?: "Failed to create ingredient") }
            )
        }
    }

    fun updateIngredient(
        id: Int,
        reorderLevel: Int,
        costPerUnit: Double,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.updateIngredient(id, reorderLevel, costPerUnit)
            result.fold(
                onSuccess = {
                    onResult(true, it.message)
                    loadIngredients()
                },
                onFailure = { onResult(false, it.message ?: "Failed to update ingredient") }
            )
        }
    }

    fun deleteIngredient(
        id: Int,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.deleteIngredient(id)
            result.fold(
                onSuccess = {
                    onResult(true, it.message)
                    loadIngredients()
                },
                onFailure = { onResult(false, it.message ?: "Failed to delete ingredient") }
            )
        }
    }

    fun stockIn(
        ingredientId: Int,
        quantity: Int,
        costPerUnit: Double,
        vendorName: String?,
        invoiceNumber: String?,
        notes: String?,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val req = StockInRequest(
                ingredientId = ingredientId,
                quantity = quantity,
                costPerUnit = costPerUnit,
                vendorName = vendorName,
                invoiceNumber = invoiceNumber,
                notes = notes
            )
            val result = repository.stockIn(req)
            result.fold(
                onSuccess = {
                    onResult(true, it.message)
                    loadIngredients()
                },
                onFailure = { onResult(false, it.message ?: "Failed to record stock-in") }
            )
        }
    }

    fun stockOut(
        ingredientId: Int,
        quantity: Int,
        reason: String?,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val req = StockOutRequest(
                ingredientId = ingredientId,
                quantity = quantity,
                reason = reason
            )
            val result = repository.stockOut(req)
            result.fold(
                onSuccess = {
                    onResult(true, it.message)
                    loadIngredients()
                },
                onFailure = { onResult(false, it.message ?: "Failed to record stock-out") }
            )
        }
    }

    fun stockWastage(
        ingredientId: Int,
        quantity: Int,
        reason: String?,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val req = WastageRequest(
                ingredientId = ingredientId,
                quantity = quantity,
                reason = reason
            )
            val result = repository.stockWastage(req)
            result.fold(
                onSuccess = {
                    onResult(true, it.message)
                    loadIngredients()
                },
                onFailure = { onResult(false, it.message ?: "Failed to record wastage") }
            )
        }
    }

    fun stockAdjustment(
        ingredientId: Int,
        newStock: Int,
        reason: String?,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val req = AdjustmentRequest(
                ingredientId = ingredientId,
                newStock = newStock,
                reason = reason
            )
            val result = repository.stockAdjustment(req)
            result.fold(
                onSuccess = {
                    onResult(true, it.message)
                    loadIngredients()
                },
                onFailure = { onResult(false, it.message ?: "Failed to adjust stock") }
            )
        }
    }

    fun loadLowStock(prompt: Boolean = false) {
        viewModelScope.launch {
            val prev = _uiState.value.lowStock
            val result = repository.getLowStock()
            result.fold(
                onSuccess = {
                    val shouldPrompt = if (prompt) true else prev.isEmpty() && it.isNotEmpty()
                    _uiState.value = _uiState.value.copy(lowStock = it, shouldPromptLowStock = shouldPrompt)
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(error = it.message)
                }
            )
        }
    }

    fun onLowStockDialogDismissed() {
        _uiState.value = _uiState.value.copy(shouldPromptLowStock = false)
    }
}
