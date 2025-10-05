package com.swadratna.swadratna_admin.ui.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swadratna.swadratna_admin.data.model.MenuCategory
import com.swadratna.swadratna_admin.data.model.MenuItem
import com.swadratna.swadratna_admin.data.model.ToggleAvailabilityRequest
import com.swadratna.swadratna_admin.data.model.Activity
import com.swadratna.swadratna_admin.data.model.ActivityType
import com.swadratna.swadratna_admin.data.remote.toDomain
import com.swadratna.swadratna_admin.data.repository.MenuRepository
import com.swadratna.swadratna_admin.data.repository.ActivityRepository
import java.time.LocalDateTime
import java.util.UUID
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log

@HiltViewModel
class MenuManagementViewModel @Inject constructor(
    private val repository: MenuRepository,
    private val activityRepository: ActivityRepository
) : ViewModel() {

    private val _categoriesState =
        MutableStateFlow<MenuCategoriesUiState>(MenuCategoriesUiState.Loading)
    val categoriesState: StateFlow<MenuCategoriesUiState> = _categoriesState.asStateFlow()

    private val _menuItemsState = MutableStateFlow<MenuUiState>(MenuUiState.Loading)
    val menuItemsState: StateFlow<MenuUiState> = _menuItemsState.asStateFlow()

    private val _selectedCategory = MutableStateFlow<MenuCategory?>(null)
    val selectedCategory: StateFlow<MenuCategory?> = _selectedCategory.asStateFlow()

    private val _isCreatingCategory = MutableStateFlow(false)
    val isCreatingCategory: StateFlow<Boolean> = _isCreatingCategory.asStateFlow()

    init {
        loadCategories()
        loadMenuItems()
    }

    fun loadCategories() {
        viewModelScope.launch {
            _categoriesState.value = MenuCategoriesUiState.Loading
            repository.getMenuCategories()
                .onSuccess { categories ->
                    _categoriesState.value = MenuCategoriesUiState.Success(categories)
                }
                .onFailure { error ->
                    _categoriesState.value =
                        MenuCategoriesUiState.Error(error.message ?: "Failed to load categories")
                }
        }
    }

    fun loadMenuItems(categoryId: Int? = null) {
        viewModelScope.launch {
            _menuItemsState.value = MenuUiState.Loading
            repository.getMenuItems(categoryId = categoryId)
                .onSuccess { response ->
                    val items = response.items?.map { it.toDomain() } ?: emptyList()
                    _menuItemsState.value =
                        MenuUiState.Success(items = items, successMessage = null)
                }
                .onFailure { error ->
                    _menuItemsState.value =
                        MenuUiState.Error(error.message ?: "Failed to load menu items")
                }
        }
    }

    fun selectCategory(category: MenuCategory?) {
        _selectedCategory.value = category
        loadMenuItems(category?.id)
    }

    fun createCategory(category: MenuCategory) {
        viewModelScope.launch {
            _isCreatingCategory.value = true
            repository.createMenuCategory(category)
                .onSuccess { response ->
                    if (response.success) {
                        loadCategories()
                        activityRepository.addActivity(
                            Activity(
                                id = UUID.randomUUID().toString(),
                                type = ActivityType.CATEGORY_CREATED,
                                title = "Category Created",
                                description = "Category '${category.name}' has been created successfully",
                                timestamp = LocalDateTime.now()
                            )
                        )
                    } else {
                        _categoriesState.value = MenuCategoriesUiState.Error(
                            response.message ?: "Error getting response"
                        )
                    }
                }
                .onFailure { error ->
                    _categoriesState.value = MenuCategoriesUiState.Error(
                        error.message ?: "Failed to create category"
                    )
                }
            _isCreatingCategory.value = false
        }
    }

    fun toggleAvailability(item: MenuItem) {
        viewModelScope.launch {
            val itemId = item.id ?: return@launch
            val request = ToggleAvailabilityRequest(!item.isAvailable)

            repository.toggleMenuItemAvailability(itemId, request)
                .onSuccess { response ->
                    val isSuccessful = response.success || response.message.contains(
                        "successfully",
                        ignoreCase = true
                    )
                    if (isSuccessful) {
                        val current = (_menuItemsState.value as? MenuUiState.Success)?.items
                            ?: return@onSuccess
                        val updated = current.map {
                            if (it.id == item.id) it.copy(isAvailable = !it.isAvailable) else it
                        }
                        val statusText = if (!item.isAvailable) "enabled" else "disabled"
                        _menuItemsState.value = MenuUiState.Success(
                            items = updated,
                            successMessage = "Menu item availability ${statusText} successfully"
                        )
                    } else {
                        _menuItemsState.value = MenuUiState.Error(
                            response.message
                        )
                    }
                }
                .onFailure { error ->
                    _menuItemsState.value = MenuUiState.Error(
                        error.message ?: "Failed to toggle availability"
                    )
                }
        }
    }

    fun clearSuccessMessage() {
        val currentState = _menuItemsState.value
        if (currentState is MenuUiState.Success) {
            _menuItemsState.value = currentState.copy(successMessage = null)
        }
    }
}

sealed class MenuCategoriesUiState {
    object Loading : MenuCategoriesUiState()
    data class Success(val categories: List<MenuCategory>) : MenuCategoriesUiState()
    data class Error(val message: String) : MenuCategoriesUiState()
}