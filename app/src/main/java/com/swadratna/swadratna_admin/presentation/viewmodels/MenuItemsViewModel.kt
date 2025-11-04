package com.swadratna.swadratna_admin.presentation.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swadratna.swadratna_admin.data.model.MenuItem
import com.swadratna.swadratna_admin.data.model.MenuCategory
import com.swadratna.swadratna_admin.data.model.CreateMenuItemRequest
import com.swadratna.swadratna_admin.data.model.UpdateMenuItemRequest
import com.swadratna.swadratna_admin.data.model.ToggleAvailabilityRequest
import com.swadratna.swadratna_admin.data.model.Activity
import com.swadratna.swadratna_admin.data.model.ActivityType
import com.swadratna.swadratna_admin.data.repository.MenuRepository
import com.swadratna.swadratna_admin.data.repository.ActivityRepository
import java.time.LocalDateTime
import java.util.UUID
import com.swadratna.swadratna_admin.data.remote.toDomain
import com.swadratna.swadratna_admin.ui.menu.MenuCategoriesUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class MenuItemsViewModel @Inject constructor(
    private val repository: MenuRepository,
    private val activityRepository: ActivityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MenuItemsUiState())
    val uiState: StateFlow<MenuItemsUiState> = _uiState.asStateFlow()

    private val _categoriesState =
        MutableStateFlow<MenuCategoriesUiState>(MenuCategoriesUiState.Loading)
    val categoriesState: StateFlow<MenuCategoriesUiState> = _categoriesState.asStateFlow()

    private val _isCreatingMenuItem = MutableStateFlow(false)
    val isCreatingMenuItem: StateFlow<Boolean> = _isCreatingMenuItem.asStateFlow()

    private val _isUpdatingMenuItem = MutableStateFlow(false)
    val isUpdatingMenuItem: StateFlow<Boolean> = _isUpdatingMenuItem.asStateFlow()

    // Selected item for edit screen: fetched from full menu endpoint to ensure all fields are populated
    private val _selectedMenuItem = MutableStateFlow<MenuItem?>(null)
    val selectedMenuItem: StateFlow<MenuItem?> = _selectedMenuItem.asStateFlow()

    init {
        loadCategories()
        loadMenuItems()
    }

    fun loadMenuItems(
        categoryId: Int? = null,
        isAvailable: Boolean? = null,
        search: String? = null,
        page: Int = 1,
        limit: Int = 20
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val aggregatedItems = mutableListOf<MenuItem>()
            var currentPage = page
            var total: Int? = null

            while (true) {
                val result = repository.getMenuItems(
                    categoryId = categoryId,
                    isAvailable = isAvailable,
                    search = search,
                    page = currentPage,
                    limit = limit
                )

                val response = result.getOrElse { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to load menu items"
                    )
                    return@launch
                }

                val items = response.items?.map { it.toDomain() } ?: emptyList()
                aggregatedItems.addAll(items)

                // Capture total from server if available
                if (total == null) {
                    total = response.pagination?.total
                }

                val hasMore = items.isNotEmpty() && (total == null || aggregatedItems.size < total!!)
                if (!hasMore) break

                currentPage += 1
            }

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                menuItems = aggregatedItems,
                total = total ?: aggregatedItems.size,
                currentPage = 1,
                limit = limit,
                selectedCategoryId = categoryId,
                searchQuery = search ?: "",
                availabilityFilter = isAvailable
            )
        }
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

    // Fetch a single menu item with full details (including discountPercentage and displayOrder)
    fun loadMenuItemById(menuItemId: Long) {
        viewModelScope.launch {
            repository.getMenu(categoryId = null)
                .onSuccess { fullMenuItems ->
                    val found = fullMenuItems.find { it.id?.toLong() == menuItemId }
                    _selectedMenuItem.value = found
                }
                .onFailure { error ->
                    Log.e("MenuItemsViewModel", "Failed to load full menu for item $menuItemId: ${error.message}")
                }
        }
    }

    fun createMenuItem(menuItem: CreateMenuItemRequest) {
        viewModelScope.launch {
            _isCreatingMenuItem.value = true
            repository.createMenuItem(menuItem)
                .onSuccess { response ->
                    val isSuccessful = response.success || (response.message?.contains("success", ignoreCase = true) == true)
                    if (isSuccessful) {
                        activityRepository.addActivity(
                            Activity(
                                id = UUID.randomUUID().toString(),
                                type = ActivityType.MENU_ITEM_CREATED,
                                title = "Menu Item Added",
                                description = "Menu item '${menuItem.name}' has been successfully added",
                                timestamp = LocalDateTime.now()
                            )
                        )

                        _uiState.value = _uiState.value.copy(
                            selectedCategoryId = null,
                            successMessage = "Menu item '${menuItem.name}' created successfully"
                        )
                        _isCreatingMenuItem.value = false
                        
                        viewModelScope.launch {
                            repository.getMenuItems(
                                categoryId = null,
                                isAvailable = _uiState.value.availabilityFilter,
                                search = _uiState.value.searchQuery.takeIf { it.isNotBlank() },
                                page = 1,
                                limit = _uiState.value.limit
                            ).onSuccess { menuItemsResponse ->
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    menuItems = menuItemsResponse.items?.map { it.toDomain() } ?: emptyList(),
                                    total = menuItemsResponse.pagination?.total ?: 0,
                                    currentPage = 1
                                )
                            }.onFailure { error ->
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    error = error.message ?: "Failed to refresh menu items"
                                )
                            }
                        }
                    } else {
                        _uiState.value = _uiState.value.copy(
                            error = response.message
                        )
                        _isCreatingMenuItem.value = false
                    }
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message ?: "Failed to create menu item"
                    )
                    _isCreatingMenuItem.value = false
                }
            _isCreatingMenuItem.value = false
        }
    }

    fun updateMenuItem(id: Int, menuItem: UpdateMenuItemRequest) {
        viewModelScope.launch {
            _isUpdatingMenuItem.value = true
            repository.updateMenuItem(id, menuItem)
                .onSuccess { response ->
                    if (response.success) {
                        activityRepository.addActivity(
                            Activity(
                                id = UUID.randomUUID().toString(),
                                type = ActivityType.MENU_ITEM_UPDATED,
                                title = "Menu Item Updated",
                                description = "Menu item '${menuItem.name}' has been successfully updated",
                                timestamp = LocalDateTime.now()
                            )
                        )

                        _uiState.value = _uiState.value.copy(
                            selectedCategoryId = null
                        )
                        loadMenuItems(
                            categoryId = null,
                            isAvailable = _uiState.value.availabilityFilter,
                            search = _uiState.value.searchQuery.takeIf { it.isNotBlank() },
                            page = 1, // Reset to first page
                            limit = _uiState.value.limit
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            error = response.message
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message ?: "Failed to update menu item"
                    )
                }
            _isUpdatingMenuItem.value = false
        }
    }

    fun toggleMenuItemAvailability(menuItem: MenuItem) {
        viewModelScope.launch {
            val request = ToggleAvailabilityRequest(!menuItem.isAvailable)
            repository.toggleMenuItemAvailability(menuItem.id ?: return@launch, request)
                .onSuccess { response ->
                    val isSuccessful = response.success || (response.message?.contains(
                        "successfully",
                        ignoreCase = true
                    ) == true)
                    if (isSuccessful) {
                        val statusText = if (!menuItem.isAvailable) "enabled" else "disabled"

                        viewModelScope.launch {
                            activityRepository.addActivity(
                                Activity(
                                    id = UUID.randomUUID().toString(),
                                    type = ActivityType.MENU_ITEM_AVAILABILITY_CHANGED,
                                    title = "Menu Item Availability Changed",
                                    description = "Menu item '${menuItem.name}' availability has been ${statusText}",
                                    timestamp = LocalDateTime.now()
                                )
                            )
                        }

                        val updatedItems = _uiState.value.menuItems.map { item ->
                            if (item.id == menuItem.id) {
                                item.copy(isAvailable = !item.isAvailable)
                            } else {
                                item
                            }
                        }
                        _uiState.value = _uiState.value.copy(
                            menuItems = updatedItems,
                            successMessage = "Menu item availability ${statusText} successfully",
                            error = null
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            error = response.message,
                            successMessage = null
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message ?: "Failed to toggle availability",
                        successMessage = null
                    )
                }
        }
    }

    fun deleteMenuItem(menuItem: MenuItem) {
        viewModelScope.launch {
            Log.d("MenuItemsViewModel", "Starting delete process for menu item: ${menuItem.name} (ID: ${menuItem.id})")

            val id = menuItem.id
            if (id == null) {
                Log.e("MenuItemsViewModel", "Cannot delete menu item: ID is null")
                _uiState.value = _uiState.value.copy(
                    error = "Cannot delete menu item: Invalid ID",
                    successMessage = null
                )
                return@launch
            }

            repository.deleteMenuItem(id)
                .onSuccess { response ->
                    Log.d("MenuItemsViewModel", "Menu item deleted successfully via API: ${menuItem.name} (ID: $id)")

                    // Track activity for delete
                    activityRepository.addActivity(
                        Activity(
                            id = UUID.randomUUID().toString(),
                            type = ActivityType.MENU_ITEM_DELETED,
                            title = "Menu Item Deleted",
                            description = "Menu item '${menuItem.name}' has been deleted successfully",
                            timestamp = LocalDateTime.now()
                        )
                    )

                    // Remove item from local state
                    val updatedItems = _uiState.value.menuItems.filter { item ->
                        item.id != id
                    }

                    val newTotal = (_uiState.value.total - 1).coerceAtLeast(0)

                    _uiState.value = _uiState.value.copy(
                        menuItems = updatedItems,
                        total = newTotal,
                        successMessage = "Menu item '${menuItem.name}' deleted successfully",
                        error = null
                    )

                    Log.d("MenuItemsViewModel", "Menu item deleted successfully. Remaining items: ${updatedItems.size}")
                }
                .onFailure { error ->
                    Log.e("MenuItemsViewModel", "Failed to delete menu item via API: ${error.message}", error)
                    _uiState.value = _uiState.value.copy(
                        error = error.message ?: "Failed to delete menu item",
                        successMessage = null
                    )
                }
        }
    }

    fun filterByCategory(categoryId: Int?) {
        _uiState.value = _uiState.value.copy(
            currentPage = 1,
            selectedCategoryId = categoryId
        )
        loadMenuItems(
            categoryId = categoryId,
            isAvailable = _uiState.value.availabilityFilter,
            search = _uiState.value.searchQuery.takeIf { it.isNotBlank() },
            page = 1, // Reset to first page when filtering
            limit = _uiState.value.limit
        )
    }

    fun filterByAvailability(isAvailable: Boolean?) {
        loadMenuItems(
            categoryId = _uiState.value.selectedCategoryId,
            isAvailable = isAvailable,
            search = _uiState.value.searchQuery.takeIf { it.isNotBlank() },
            page = 1, // Reset to first page when filtering
            limit = _uiState.value.limit
        )
    }

    fun searchMenuItems(query: String) {
        loadMenuItems(
            categoryId = _uiState.value.selectedCategoryId,
            isAvailable = _uiState.value.availabilityFilter,
            search = query.takeIf { it.isNotBlank() },
            page = 1, // Reset to first page when searching
            limit = _uiState.value.limit
        )
    }

    fun loadNextPage() {
        if (_uiState.value.currentPage * _uiState.value.limit < _uiState.value.total) {
            loadMenuItems(
                categoryId = _uiState.value.selectedCategoryId,
                isAvailable = _uiState.value.availabilityFilter,
                search = _uiState.value.searchQuery.takeIf { it.isNotBlank() },
                page = _uiState.value.currentPage + 1,
                limit = _uiState.value.limit
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }
}

data class MenuItemsUiState(
    val isLoading: Boolean = false,
    val menuItems: List<MenuItem> = emptyList(),
    val total: Int = 0,
    val currentPage: Int = 1,
    val limit: Int = 20,
    val selectedCategoryId: Int? = null,
    val searchQuery: String = "",
    val availabilityFilter: Boolean? = null,
    val error: String? = null,
    val successMessage: String? = null
)