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
                        val errorMessage = response.message ?: "Category creation failed - no error message provided"
                        Log.d("MenuManagementViewModel", "Category creation failed: going to else condition with message='$errorMessage'")
                        _categoriesState.value = MenuCategoriesUiState.Error(errorMessage)
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

    fun clearCategorySuccessMessage() {
        val currentState = _categoriesState.value
        if (currentState is MenuCategoriesUiState.Success) {
            _categoriesState.value = currentState.copy(successMessage = null)
        }
    }

    fun deleteCategory(category: MenuCategory) {
        viewModelScope.launch {
            Log.d("MenuManagementViewModel", "Attempting to delete category: ${category.name} (ID: ${category.id})")
            
            // Check if category has a valid ID
            val categoryId = category.id
            if (categoryId == null) {
                Log.e("MenuManagementViewModel", "Cannot delete category: ID is null")
                _categoriesState.value = MenuCategoriesUiState.Error("Cannot delete category: Invalid ID")
                return@launch
            }
            
            try {
                // Call the repository to delete the category
                val result = repository.deleteCategory(categoryId)
                
                result.fold(
                    onSuccess = { response ->
                        Log.d("MenuManagementViewModel", "Category deleted successfully via API")
                        
                        // Add activity tracking
                        activityRepository.addActivity(
                            Activity(
                                id = UUID.randomUUID().toString(),
                                type = ActivityType.CATEGORY_DELETED,
                                title = "Category Deleted",
                                description = "Category '${category.name}' has been deleted successfully",
                                timestamp = LocalDateTime.now()
                            )
                        )
                        
                        // Remove from local state
                        val currentState = _categoriesState.value
                        if (currentState is MenuCategoriesUiState.Success) {
                            val updatedCategories = currentState.categories.filter { it.id != category.id }
                            _categoriesState.value = MenuCategoriesUiState.Success(
                                categories = updatedCategories,
                                successMessage = "Category '${category.name}' deleted successfully"
                            )
                        }
                    },
                    onFailure = { error ->
                        Log.e("MenuManagementViewModel", "Failed to delete category: ${error.message}", error)
                        _categoriesState.value = MenuCategoriesUiState.Error(
                            "Failed to delete category: ${error.message ?: "Unknown error"}"
                        )
                    }
                )
                
            } catch (e: Exception) {
                Log.e("MenuManagementViewModel", "Failed to delete category: ${e.message}", e)
                _categoriesState.value = MenuCategoriesUiState.Error(
                    "Failed to delete category: ${e.message ?: "Unknown error"}"
                )
            }
        }
    }

    fun toggleCategoryAvailability(category: MenuCategory) {
        viewModelScope.launch {
            Log.d("MenuManagementViewModel", "Toggling availability for category: ${category.name} (current: ${category.isActive})")
            
            // Check if category has a valid ID
            val categoryId = category.id
            if (categoryId == null) {
                Log.e("MenuManagementViewModel", "Cannot toggle category availability: ID is null")
                _categoriesState.value = MenuCategoriesUiState.Error("Cannot toggle category availability: Invalid ID")
                return@launch
            }
            
            try {
                val newAvailability = !category.isActive
                val toggleRequest = ToggleAvailabilityRequest(isAvailable = newAvailability)
                
                // Call the repository to toggle category availability
                val result = repository.toggleCategoryAvailability(categoryId, toggleRequest)
                
                result.fold(
                    onSuccess = { response ->
                        Log.d("MenuManagementViewModel", "Category availability toggled successfully via API")
                        
                        // Add activity tracking
                        activityRepository.addActivity(
                            Activity(
                                id = UUID.randomUUID().toString(),
                                type = ActivityType.CATEGORY_UPDATED,
                                title = "Category Availability Updated",
                                description = "Category '${category.name}' has been ${if (newAvailability) "activated" else "deactivated"}",
                                timestamp = LocalDateTime.now()
                            )
                        )
                        
                        // Update local state
                        val currentState = _categoriesState.value
                        if (currentState is MenuCategoriesUiState.Success) {
                            val updatedCategories = currentState.categories.map { cat ->
                                if (cat.id == category.id) {
                                    cat.copy(isActive = newAvailability)
                                } else {
                                    cat
                                }
                            }
                            val statusText = if (newAvailability) "activated" else "deactivated"
                            _categoriesState.value = MenuCategoriesUiState.Success(
                                categories = updatedCategories,
                                successMessage = "Category '${category.name}' ${statusText} successfully"
                            )
                        }
                    },
                    onFailure = { error ->
                        Log.e("MenuManagementViewModel", "Failed to toggle category availability: ${error.message}", error)
                        _categoriesState.value = MenuCategoriesUiState.Error(
                            "Failed to toggle category availability: ${error.message ?: "Unknown error"}"
                        )
                    }
                )
                
            } catch (e: Exception) {
                Log.e("MenuManagementViewModel", "Failed to toggle category availability: ${e.message}", e)
                _categoriesState.value = MenuCategoriesUiState.Error(
                    "Failed to toggle category availability: ${e.message ?: "Unknown error"}"
                )
            }
        }
    }
}

sealed class MenuCategoriesUiState {
    object Loading : MenuCategoriesUiState()
    data class Success(val categories: List<MenuCategory>, val successMessage: String? = null) : MenuCategoriesUiState()
    data class Error(val message: String) : MenuCategoriesUiState()
}