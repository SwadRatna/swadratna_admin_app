package com.swadratna.swadratna_admin.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.swadratna.swadratna_admin.ui.analytics.AdminAnalyticsScreen
import com.swadratna.swadratna_admin.ui.dashboard.DashboardScreen
import com.swadratna.swadratna_admin.ui.home.HomeScreen
import com.swadratna.swadratna_admin.ui.campaign.CampaignScreen
import com.swadratna.swadratna_admin.ui.campaign.CreateCampaignScreen
import com.swadratna.swadratna_admin.ui.screens.LoginScreen
import com.swadratna.swadratna_admin.ui.settings.SettingsScreen
import com.swadratna.swadratna_admin.ui.staff.AddStaffScreen
import com.swadratna.swadratna_admin.ui.staff.EditStaffScreen
import com.swadratna.swadratna_admin.ui.staff.StaffManagementScreen
import com.swadratna.swadratna_admin.ui.store.CreateStoreScreen
import com.swadratna.swadratna_admin.ui.store.StoreScreen
import com.swadratna.swadratna_admin.ui.store.StoreDetailScreen
import com.swadratna.swadratna_admin.ui.attendance.AttendancePaymentScreen
import com.swadratna.swadratna_admin.ui.menu.MenuScreen
import com.swadratna.swadratna_admin.ui.menu.MenuManagementScreen
import com.swadratna.swadratna_admin.ui.menu.AddCategoryScreen
import com.swadratna.swadratna_admin.ui.menu.AddMenuScreen
import com.swadratna.swadratna_admin.presentation.screens.menu.MenuItemsScreen
import com.swadratna.swadratna_admin.presentation.screens.menu.AddMenuItemScreen
import com.swadratna.swadratna_admin.presentation.screens.menu.EditMenuItemScreen
import com.swadratna.swadratna_admin.presentation.screens.menu.ManageCategoriesScreen
import com.swadratna.swadratna_admin.ui.notifications.NotificationScreen
import com.swadratna.swadratna_admin.ui.viewmodels.AuthViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = NavRoute.Login.route,
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsState()
    
    // Handle session expiration
    LaunchedEffect(authState.sessionExpired) {
        if (authState.sessionExpired) {
            navController.navigate(NavRoute.Login.route) {
                popUpTo(0) { inclusive = true }
            }
            authViewModel.resetSessionExpired()
        }
    }
    
    // Determine the actual start destination based on authentication state
    val actualStartDestination = if (authState.isLoading) {
        NavRoute.Login.route // Show login while loading
    } else if (authState.isAuthenticated) {
        NavRoute.Dashboard.route
    } else {
        NavRoute.Login.route
    }
    
    NavHost(
        navController = navController,
        startDestination = actualStartDestination,
        modifier = modifier
    ) {
        composable(NavRoute.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(NavRoute.Dashboard.route) {
                        popUpTo(NavRoute.Login.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(NavRoute.Dashboard.route) {
            DashboardScreen(
                onNavigateToSettings = {
                    navController.navigate(NavRoute.Settings.route)
                },
                onNavigateToNotifications = {
                    navController.navigate(NavRoute.Notifications.route)
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
                    navController.navigate(NavRoute.StaffManagement.createRoute(selectedStoreId))
                },
                onNavigateToMenuManagement = { selectedStoreId ->
                    navController.navigate(NavRoute.MenuManagement.route)
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
            AdminAnalyticsScreen()
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
                    authViewModel.logout()
                    navController.navigate(NavRoute.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        composable(NavRoute.StaffManagement.route) { backStackEntry ->
            val storeId = backStackEntry.arguments?.getString("storeId") ?: ""
            StaffManagementScreen(
                storeId = storeId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddStaff = { navController.navigate(NavRoute.AddStaff.createRoute(storeId)) },
                onNavigateToEditStaff = { staffId -> 
                    navController.navigate(NavRoute.EditStaff.createRoute(staffId, storeId))
                }
            )
        }

        composable(NavRoute.Menu.route) {
            MenuScreen(
                onBack = { navController.popBackStack() },
                onNavigateToAddMenu = { navController.navigate(NavRoute.AddMenu.route) }
            )
        }
        
        composable(NavRoute.MenuManagement.route) {
            MenuManagementScreen(
                onBack = { navController.popBackStack() },
                onNavigateToAddMenu = { navController.navigate(NavRoute.AddMenu.route) },
                onNavigateToMenuItems = { navController.navigate(NavRoute.MenuItems.route) },
                onNavigateToManageCategories = { navController.navigate(NavRoute.ManageCategories.route) }
            )
        }
        
        composable(NavRoute.ManageCategories.route) {
            ManageCategoriesScreen(
                onBack = { navController.popBackStack() },
                onNavigateToAddCategory = { navController.navigate(NavRoute.AddCategory.route) }
            )
        }
        
        composable(NavRoute.AddCategory.route) {
            AddCategoryScreen(
                onBack = { navController.popBackStack() },
                onCategoryAdded = { navController.popBackStack() }
            )
        }
        
        composable(NavRoute.AddMenu.route) {
            AddMenuScreen(
                onBack = { navController.popBackStack() },
                onMenuAdded = { navController.popBackStack() }
            )
        }
        
        composable(NavRoute.MenuItems.route) {
            MenuItemsScreen(
                onBack = { navController.popBackStack() },
                onNavigateToAddMenuItem = { navController.navigate(NavRoute.AddMenuItem.route) }
            )
        }
        
        composable(NavRoute.AddMenuItem.route) {
            AddMenuItemScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(
            route = NavRoute.EditMenuItem.route,
            arguments = listOf(navArgument("menuItemId") { type = NavType.LongType })
        ) { backStackEntry ->
            val menuItemId = backStackEntry.arguments?.getLong("menuItemId") ?: 0L
            EditMenuItemScreen(
                menuItemId = menuItemId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(
            route = NavRoute.AddStaff.route,
            arguments = listOf(navArgument("storeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val storeId = backStackEntry.arguments?.getString("storeId") ?: ""
            AddStaffScreen(
                onNavigateBack = { navController.popBackStack() },
                storeId = storeId
            )
        }
        
        composable(
            route = NavRoute.EditStaff.route,
            arguments = listOf(
                navArgument("staffId") { type = NavType.IntType },
                navArgument("storeId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val staffId = backStackEntry.arguments?.getInt("staffId")?.toString() ?: "0"
            val storeId = backStackEntry.arguments?.getString("storeId") ?: ""
            EditStaffScreen(
                staffId = staffId,
                storeId = storeId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(NavRoute.AttendancePayment.route) {
            AttendancePaymentScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(NavRoute.Notifications.route) {
            NotificationScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

//sealed class NavRoute(val route: String) {
//    object Dashboard : NavRoute("dashboard")
//    object Home : NavRoute("home")
//    object Campaigns : NavRoute("campaigns")
//    object CreateCampaign : NavRoute("create_campaign")
//    object Store : NavRoute("store")
//    object CreateStore : NavRoute("create_store")
//    object StoreDetail : NavRoute("store_detail")
//    object Analytics : NavRoute("analytics")
//    object Settings : NavRoute("settings")
//    object StaffManagement : NavRoute("staff_management")
//    object AddStaff : NavRoute("add_staff")
//    object AttendancePayment : NavRoute("attendance_payment")
//    object MenuScreen: NavRoute("menu_screen")
//}