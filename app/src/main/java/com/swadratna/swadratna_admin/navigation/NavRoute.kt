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
    object StaffManagement : NavRoute("staff_management")
    object AddStaff : NavRoute("add_staff")
    object AttendancePayment : NavRoute("attendance_payment")
    object Menu : NavRoute("menu")
}