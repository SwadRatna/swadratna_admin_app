package com.swadratna.swadratna_admin.data.model

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class Activity(
    val id: String,
    val type: ActivityType,
    val title: String,
    val description: String,
    val timestamp: LocalDateTime,
    val entityId: String? = null,
    val entityName: String? = null
) {
    fun getFormattedTime(): String {
        val now = LocalDateTime.now()
        val duration = java.time.Duration.between(timestamp, now)
        
        return when {
            duration.toMinutes() < 1 -> "Just now"
            duration.toMinutes() < 60 -> "${duration.toMinutes()}m ago"
            duration.toHours() < 24 -> "${duration.toHours()}h ago"
            duration.toDays() < 7 -> "${duration.toDays()}d ago"
            else -> timestamp.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
        }
    }
    
    fun getFormattedDateTime(): String {
        return timestamp.format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm"))
    }
}

enum class ActivityType(val displayName: String, val icon: String) {
    STORE_CREATED("Store Created", "store"),
    STORE_UPDATED("Store Updated", "edit"),
    STORE_DELETED("Store Deleted", "delete"),
    STAFF_CREATED("Staff Added", "person_add"),
    STAFF_UPDATED("Staff Updated", "person"),
    STAFF_DELETED("Staff Removed", "person_remove"),
    MENU_ITEM_CREATED("Menu Item Added", "restaurant_menu"),
    MENU_ITEM_UPDATED("Menu Item Updated", "edit"),
    MENU_ITEM_DELETED("Menu Item Removed", "delete"),
    MENU_ITEM_AVAILABILITY_CHANGED("Menu Item Availability Changed", "visibility"),
    CATEGORY_CREATED("Category Added", "category"),
    CATEGORY_UPDATED("Category Updated", "edit"),
    CATEGORY_DELETED("Category Removed", "delete");
    
    fun getColor(): Long {
        return when (this) {
            STORE_CREATED, STAFF_CREATED, MENU_ITEM_CREATED, CATEGORY_CREATED -> 0xFF4CAF50 // Green
            STORE_UPDATED, STAFF_UPDATED, MENU_ITEM_UPDATED, CATEGORY_UPDATED, MENU_ITEM_AVAILABILITY_CHANGED -> 0xFF2196F3 // Blue
            STORE_DELETED, STAFF_DELETED, MENU_ITEM_DELETED, CATEGORY_DELETED -> 0xFFF44336 // Red
        }
    }
}