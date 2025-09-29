package com.swadratna.swadratna_admin.data.repository

import com.swadratna.swadratna_admin.data.model.MenuItem
import com.swadratna.swadratna_admin.data.model.MenuCategory
import com.swadratna.swadratna_admin.data.remote.api.MenuApi
import com.swadratna.swadratna_admin.data.remote.toDomain
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MenuRepository @Inject constructor(
    private val api: MenuApi
) {
    var useMock: Boolean = false

    suspend fun getMenu(category: MenuCategory): List<MenuItem> = withContext(Dispatchers.IO) {
        if (useMock) return@withContext dummyMenuItemsFor(category)

        runCatching {
            val categoryParam = category.takeIf { it != MenuCategory.ALL }?.name
            api.getMenu(categoryParam).map { it.toDomain() }
        }.getOrElse {
            dummyMenuItemsFor(category)
        }
    }

    private fun dummyMenuItemsFor(category: MenuCategory): List<MenuItem> {
        val all = listOf(
            MenuItem(1, "Classic Margherita Pizza", "Fresh tomato sauce, mozzarella, and basil on", 14.99, "All Day", true),
            MenuItem(2, "Caesar Salad", "Crisp romaine lettuce, croutons, parmesan,", 9.50, "Lunch & Dinner", true),
            MenuItem(3, "Chocolate Lava Cake", "Warm chocolate cake with a molten center,", 8.00, "Dinner Only", false),
            MenuItem(4, "Freshly Squeezed Lemonade", "Classic tangy and sweet lemonade, perfectly", 4.25, "All Day", true)
        )
        return when (category) {
            MenuCategory.ALL -> all
            MenuCategory.APPETIZERS -> all.filter { it.id <= 2 }
            MenuCategory.MAIN -> all.filter { it.id > 2 }
        }
    }
}
