package com.swadratna.swadratna_admin.data.model

data class MenuItem(
    val id: Int,
    val name: String,
    val description: String,
    val price: Double,
    val availability: String,
    var isAvailable: Boolean
)

enum class MenuCategory(val displayName: String) {
    ALL("All Items"),
    APPETIZERS("Appetizers"),
    MAIN("Main Courses")
}
