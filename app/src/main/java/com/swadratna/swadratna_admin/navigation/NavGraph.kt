package com.swadratna.swadratna_admin.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.swadratna.swadratna_admin.ui.dashboard.DashboardScreen
import com.swadratna.swadratna_admin.ui.home.HomeScreen
import com.swadratna.swadratna_admin.ui.campaign.CampaignScreen
import com.swadratna.swadratna_admin.ui.campaign.CreateCampaignScreen
import com.swadratna.swadratna_admin.ui.settings.SettingsScreen
import com.swadratna.swadratna_admin.ui.staff.AddStaffScreen
import com.swadratna.swadratna_admin.ui.staff.StaffManagementScreen
import com.swadratna.swadratna_admin.ui.store.CreateStoreScreen
import com.swadratna.swadratna_admin.ui.store.StoreScreen
import com.swadratna.swadratna_admin.ui.store.StoreDetailScreen
import com.swadratna.swadratna_admin.ui.attendance.AttendancePaymentScreen
import com.swadratna.swadratna_admin.ui.analytics.AnalyticsScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = NavRoute.Dashboard.route,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(NavRoute.Dashboard.route) {
            DashboardScreen(
                onNavigateToSettings = {
                    navController.navigate(NavRoute.Settings.route)
                }
            )
        }
        composable(NavRoute.Home.route) {
            HomeScreen()
        }
        composable(NavRoute.Campaigns.route) {
            CampaignScreen(
                onNavigateToDetails = { campaignId ->
                    // TODO: Navigate to campaign details when implemented
                },
                onNavigateToCreateCampaign = {
                    navController.navigate(NavRoute.CreateCampaign.route)
                },
                onNavigateToEditCampaign = { campaignId ->
                    navController.navigate("${NavRoute.CreateCampaign.route}/$campaignId")
                }
            )
        }
        
        composable(NavRoute.CreateCampaign.route) {
            CreateCampaignScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(
            route = "${NavRoute.CreateCampaign.route}/{campaignId}",
            arguments = listOf(navArgument("campaignId") { type = NavType.StringType })
        ) { backStackEntry ->
            val campaignId = backStackEntry.arguments?.getString("campaignId")
            CreateCampaignScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(NavRoute.Store.route) {
            StoreScreen(
                onNavigateToCreateStore = {
                    navController.navigate(NavRoute.CreateStore.route)
                },
                onNavigateToManageStore = { storeId ->
                    navController.navigate("${NavRoute.StoreDetail.route}/$storeId")
                },
                onNavigateToEditStore = { storeId ->
                    navController.navigate("${NavRoute.CreateStore.route}/$storeId")
                }
            )
        }
        
        composable(
            route = "${NavRoute.StoreDetail.route}/{storeId}",
            arguments = listOf(navArgument("storeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val storeId = backStackEntry.arguments?.getString("storeId") ?: return@composable
            StoreDetailScreen(
                storeId = storeId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToStaffManagement = { selectedStoreId ->
                    navController.navigate(NavRoute.StaffManagement.route)
                },
                onNavigateToMenuManagement = { selectedStoreId ->
                    // TODO: Implement Menu Management screen
                    navController.navigate(NavRoute.Home.route)
                },
                onNavigateToAttendance = { selectedStoreId ->
                    navController.navigate(NavRoute.AttendancePayment.route)
                }
            )
        }
        
        composable(NavRoute.CreateStore.route) {
            CreateStoreScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(
            route = "${NavRoute.CreateStore.route}/{storeId}",
            arguments = listOf(navArgument("storeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val storeId = backStackEntry.arguments?.getString("storeId")
            CreateStoreScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                storeId = storeId
            )
        }
        composable(NavRoute.Analytics.route) {
            // TODO: Add Analytics screen
            HomeScreen()
        }
        
        composable(NavRoute.Analytics.route) {
            AnalyticsScreen()
        }
        
        composable(NavRoute.Settings.route) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToCampaigns = {
                    navController.navigate(NavRoute.Campaigns.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                },
                onNavigateToStore = {
                    navController.navigate(NavRoute.Store.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                },
                onNavigateToAnalytics = {
                    navController.navigate(NavRoute.Analytics.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                },
                onLogout = {
                    // Handle logout logic here
                    navController.navigate(NavRoute.Dashboard.route) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                }
            )
        }
        
        composable(NavRoute.StaffManagement.route) {
            StaffManagementScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddStaff = { navController.navigate(NavRoute.AddStaff.route) }
            )
        }
        
        composable(NavRoute.AddStaff.route) {
            AddStaffScreen(
                onNavigateBack = { navController.popBackStack() },
                storeId = "" // This would typically come from the StaffManagementScreen
            )
        }
        
        composable(NavRoute.AttendancePayment.route) {
            AttendancePaymentScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

sealed class NavRoute(val route: String) {
    object Dashboard : NavRoute("dashboard")
    object Home : NavRoute("home")
    object Campaigns : NavRoute("campaigns")
    object CreateCampaign : NavRoute("create_campaign")
    object Store : NavRoute("store")
    object CreateStore : NavRoute("create_store")
    object StoreDetail : NavRoute("store_detail")
    object Analytics : NavRoute("analytics")
    object Settings : NavRoute("settings")
    object StaffManagement : NavRoute("staff_management")
    object AddStaff : NavRoute("add_staff")
    object AttendancePayment : NavRoute("attendance_payment")
}