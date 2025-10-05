package com.swadratna.swadratna_admin.navigation

sealed class NavRoute(val route: String) {
    object Login : NavRoute("login")
    object Dashboard : NavRoute("dashboard")
    object Home : NavRoute("home")
    object Campaigns : NavRoute("campaigns")
    object CreateCampaign : NavRoute("create_campaign")
    object Store : NavRoute("store")
    object CreateStore : NavRoute("create_store")
    object StoreDetail : NavRoute("store_detail")
    object Settings : NavRoute("settings")
    object Analytics : NavRoute("analytics")
    object StaffManagement : NavRoute("staff_management/{storeId}") {
        fun createRoute(storeId: String) = "staff_management/$storeId"
    }
    object AddStaff : NavRoute("add_staff/{storeId}") {
        fun createRoute(storeId: String) = "add_staff/$storeId"
    }
    object EditStaff : NavRoute("edit_staff/{staffId}/{storeId}") {
        fun createRoute(staffId: Int, storeId: String) = "edit_staff/$staffId/$storeId"
    }
    object AttendancePayment : NavRoute("attendance_payment")
    object Menu : NavRoute("menu")
    object MenuManagement : NavRoute("menu_management")
    object AddCategory : NavRoute("add_category")
    object AddMenu : NavRoute("add_menu")
    object MenuItems : NavRoute("menu_items")
    object AddMenuItem : NavRoute("add_menu_item")
    object EditMenuItem : NavRoute("edit_menu_item/{menuItemId}") {
        fun createRoute(menuItemId: Long) = "edit_menu_item/$menuItemId"
    }
    object Notifications : NavRoute("notifications")
}