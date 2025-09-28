package com.swadratna.swadratna_admin.ui.menu

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.swadratna.swadratna_admin.data.model.MenuCategory
import com.swadratna.swadratna_admin.data.model.MenuItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MenuViewModel : ViewModel() {

    private val _selectedCategory = mutableStateOf(MenuCategory.ALL)
    val selectedCategory get() = _selectedCategory

    private val _menuItems = MutableStateFlow<List<MenuItem>>(dummyMenuItems())
    val menuItems: StateFlow<List<MenuItem>> get() = _menuItems

    fun selectCategory(category: MenuCategory) {
        _selectedCategory.value = category
        // For now, we can filter locally if needed
        _menuItems.value = dummyMenuItems().filter {
            when(category) {
                MenuCategory.ALL -> true
                MenuCategory.APPETIZERS -> it.id <= 2
                MenuCategory.MAIN -> it.id > 2
            }
        }
    }

    fun toggleAvailability(item: MenuItem) {
        val updatedList = _menuItems.value.map {
            if (it.id == item.id) it.copy(isAvailable = !it.isAvailable) else it
        }
        _menuItems.value = updatedList
    }

    private fun dummyMenuItems(): List<MenuItem> = listOf(
        MenuItem(1, "Classic Margherita Pizza", "Fresh tomato sauce, mozzarella, and basil on", 14.99, "All Day", true),
        MenuItem(2, "Caesar Salad", "Crisp romaine lettuce, croutons, parmesan,", 9.50, "Lunch & Dinner", true),
        MenuItem(3, "Chocolate Lava Cake", "Warm chocolate cake with a molten center,", 8.00, "Dinner Only", false),
        MenuItem(4, "Freshly Squeezed Lemonade", "Classic tangy and sweet lemonade, perfectly", 4.25, "All Day", true)
    )
}
