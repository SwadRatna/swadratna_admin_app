package com.swadratna.swadratna_admin.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swadratna.swadratna_admin.data.model.CreateIngredientRequest
import com.swadratna.swadratna_admin.data.model.Ingredient
import com.swadratna.swadratna_admin.data.model.InventoryMovement
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
    val shouldPromptLowStock: Boolean = false,
    val dailyAddedValue: Double = 0.0,
    val dailySpentValue: Double = 0.0,
    val dailyInCost: Double = 0.0,
    val dailyOutCost: Double = 0.0,
    val dailyWastageCost: Double = 0.0,
    val dailyAdjustmentNetCost: Double = 0.0,
    val movementsForIngredient: List<InventoryMovement> = emptyList(),
    val showMovementsDialog: Boolean = false
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

    fun loadDailySummary(date: java.time.LocalDate) {
        viewModelScope.launch {
            val result = repository.getMovements(page = 1, limit = 500)
            result.fold(
                onSuccess = { list ->
                    val target = date.format(java.time.format.DateTimeFormatter.ISO_DATE)
                    var added = 0.0
                    var spent = 0.0
                    var inCost = 0.0
                    var outCost = 0.0
                    var wastageCost = 0.0
                    var adjustmentNet = 0.0
                    list.forEach { m ->
                        val d = m.createdAt?.take(10)
                        if (d == target) {
                            val qty = m.quantity ?: 0
                            val ingredientCost = uiState.value.ingredients.firstOrNull { it.id == m.ingredientId }?.costPerUnit ?: 0.0
                            val cpu = m.costPerUnit ?: ingredientCost
                            val total = m.totalCost ?: (qty * cpu)
                            when (m.type?.lowercase()) {
                                "in" -> {
                                    inCost += total
                                    added += total
                                }
                                "out" -> {
                                    outCost += total
                                    spent += total
                                }
                                "wastage" -> {
                                    wastageCost += total
                                    spent += total
                                }
                                "adjustment" -> {
                                    if (qty >= 0) {
                                        adjustmentNet += total
                                        added += total
                                    } else {
                                        adjustmentNet -= kotlin.math.abs(total)
                                        spent += kotlin.math.abs(total)
                                    }
                                }
                                else -> {}
                            }
                        }
                    }
                    _uiState.value = _uiState.value.copy(
                        dailyAddedValue = added,
                        dailySpentValue = spent,
                        dailyInCost = inCost,
                        dailyOutCost = outCost,
                        dailyWastageCost = wastageCost,
                        dailyAdjustmentNetCost = adjustmentNet
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(error = it.message)
                }
            )
        }
    }

    fun loadIngredientMovements(ingredientId: Int, date: java.time.LocalDate) {
        viewModelScope.launch {
            val result = repository.getMovements(page = 1, limit = 500)
            result.fold(
                onSuccess = { list ->
                    val target = date.format(java.time.format.DateTimeFormatter.ISO_DATE)
                    val filtered = list.filter { it.ingredientId == ingredientId && (it.createdAt?.take(10) == target) }
                    _uiState.value = _uiState.value.copy(movementsForIngredient = filtered, showMovementsDialog = true)
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(error = it.message)
                }
            )
        }
    }

    fun dismissMovementsDialog() {
        _uiState.value = _uiState.value.copy(showMovementsDialog = false)
    }
}
