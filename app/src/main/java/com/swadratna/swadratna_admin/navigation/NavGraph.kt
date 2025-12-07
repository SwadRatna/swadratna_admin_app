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
import com.swadratna.swadratna_admin.presentation.screens.menu.AddMenuItemScreen
import com.swadratna.swadratna_admin.presentation.screens.menu.EditMenuItemScreen
import com.swadratna.swadratna_admin.presentation.screens.menu.ManageCategoriesScreen
import com.swadratna.swadratna_admin.presentation.screens.menu.MenuItemsScreen
import com.swadratna.swadratna_admin.presentation.viewmodels.MenuItemsViewModel
import com.swadratna.swadratna_admin.ui.analytics.AdminAnalyticsScreen
import com.swadratna.swadratna_admin.ui.campaign.CampaignEvent
import com.swadratna.swadratna_admin.ui.campaign.CampaignScreen
import com.swadratna.swadratna_admin.ui.campaign.CampaignViewModel
import com.swadratna.swadratna_admin.ui.campaign.CreateCampaignScreen
import com.swadratna.swadratna_admin.ui.dashboard.DashboardScreen
import com.swadratna.swadratna_admin.ui.home.HomeScreen
import com.swadratna.swadratna_admin.ui.menu.AddCategoryScreen
import com.swadratna.swadratna_admin.ui.menu.AddMenuScreen
import com.swadratna.swadratna_admin.ui.menu.MenuManagementScreen
import com.swadratna.swadratna_admin.ui.menu.MenuManagementViewModel
import com.swadratna.swadratna_admin.ui.menu.MenuScreen
import com.swadratna.swadratna_admin.ui.menu.MenuViewModel
import com.swadratna.swadratna_admin.ui.sales.SaleListScreen
import com.swadratna.swadratna_admin.ui.screens.LoginScreen
import com.swadratna.swadratna_admin.ui.settings.SettingsScreen
import com.swadratna.swadratna_admin.ui.staff.AddStaffScreen
import com.swadratna.swadratna_admin.ui.staff.AllStaffManagementScreen
import com.swadratna.swadratna_admin.ui.staff.EditStaffScreen
import com.swadratna.swadratna_admin.ui.staff.StaffManagementScreen
import com.swadratna.swadratna_admin.ui.store.CreateStoreScreen
import com.swadratna.swadratna_admin.ui.store.StoreDetailScreen
import com.swadratna.swadratna_admin.ui.store.StoreScreen
import com.swadratna.swadratna_admin.ui.viewmodels.AuthViewModel
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = NavRoute.Login.route,
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsState()

    // Kick off a one-time automatic completion of expired campaigns when authenticated
    val campaignsAutoVM: CampaignViewModel = hiltViewModel()
    LaunchedEffect(authState.isAuthenticated) {
        if (authState.isAuthenticated) {
            campaignsAutoVM.autoCompleteExpiredCampaignsIfNeeded()
        }
    }

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
                },
                onNavigateToAllStaffManagement = {
                    navController.navigate(NavRoute.AllStaffManagement.route)
                },
                onNavigateToSaleList = {
                    navController.navigate(NavRoute.SaleList.route)
                }
            )
        }
        composable(NavRoute.Home.route) {
            HomeScreen()
        }
        composable(NavRoute.Notifications.route) {
            com.swadratna.swadratna_admin.ui.notifications.NotificationScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(NavRoute.AttendancePayment.route) {
            com.swadratna.swadratna_admin.ui.attendance.AttendancePaymentScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(NavRoute.Campaigns.route) {
            // Share the same VM instance with CampaignScreen and observe navigation result for refresh
            val campaignsViewModel: CampaignViewModel = hiltViewModel()
            // Observe a flag set by CreateCampaign screens to trigger refresh when popping back
            val needsRefresh by (navController.currentBackStackEntry?.savedStateHandle
                ?.getStateFlow("refreshCampaigns", false)
                ?: MutableStateFlow(false))
                .collectAsState(initial = false)

            LaunchedEffect(needsRefresh) {
                if (needsRefresh) {
                    campaignsViewModel.handleEvent(CampaignEvent.RefreshData)
                    navController.currentBackStackEntry?.savedStateHandle?.set("refreshCampaigns", false)
                }
            }

            CampaignScreen(
                viewModel = campaignsViewModel,
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
                    // Signal Campaigns screen to refresh after popping back
                    navController.previousBackStackEntry?.savedStateHandle?.set("refreshCampaigns", true)
                    navController.popBackStack()
                },
                navController = navController
            )
        }

        composable(
            route = "${NavRoute.CreateCampaign.route}/{campaignId}",
            arguments = listOf(navArgument("campaignId") { type = NavType.StringType })
        ) { backStackEntry ->
            val campaignId = backStackEntry.arguments?.getString("campaignId")
            CreateCampaignScreen(
                onNavigateBack = {
                    // Signal Campaigns screen to refresh after popping back
                    navController.previousBackStackEntry?.savedStateHandle?.set("refreshCampaigns", true)
                    navController.popBackStack()
                },
                campaignId = campaignId,
                navController = navController
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

        composable(
            route = NavRoute.AddStaff.route,
            arguments = listOf(navArgument("storeId") { type = NavType.StringType })
        ) {
            val storeId = it.arguments?.getString("storeId") ?: ""
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
            val staffId = backStackEntry.arguments?.getInt("staffId") ?: 0
            val storeId = backStackEntry.arguments?.getString("storeId") ?: ""
            EditStaffScreen(
                staffId = staffId.toString(),
                storeId = storeId,
                onNavigateBack = { navController.popBackStack() },
                navController = navController
            )
        }

        composable(NavRoute.EditCategory.route, arguments = listOf(navArgument("categoryId") { type = NavType.LongType })) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getLong("categoryId") ?: 0L
            com.swadratna.swadratna_admin.ui.menu.EditCategoryScreen(
                categoryId = categoryId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(NavRoute.Menu.route) {
            val menuViewModel: MenuViewModel = hiltViewModel()
            // Observe a flag set by AddMenuScreen to trigger refresh when popping back to MenuScreen
            val needsRefresh by (navController.currentBackStackEntry?.savedStateHandle
                ?.getStateFlow("refreshMenu", false)
                ?: MutableStateFlow(false))
                .collectAsState(initial = false)

            LaunchedEffect(needsRefresh) {
                if (needsRefresh) {
                    // Reload items in MenuScreen
                    menuViewModel.selectCategory(null)
                    navController.currentBackStackEntry?.savedStateHandle?.set("refreshMenu", false)
                }
            }

            MenuScreen(
                onBack = { navController.popBackStack() },
                onNavigateToAddMenu = { navController.navigate(NavRoute.AddMenu.route) }
            )
        }

        composable(NavRoute.MenuManagement.route) {
            val menuManagementViewModel: MenuManagementViewModel = hiltViewModel()
            val needsRefresh by (navController.currentBackStackEntry?.savedStateHandle
                ?.getStateFlow("refreshMenuManagement", false)
                ?: MutableStateFlow(false))
                .collectAsState(initial = false)
            val selectedCategory by menuManagementViewModel.selectedCategory.collectAsState()

            LaunchedEffect(needsRefresh) {
                if (needsRefresh) {
                    menuManagementViewModel.loadCategories()
                    menuManagementViewModel.loadMenuItems(selectedCategory?.id)
                    navController.currentBackStackEntry?.savedStateHandle?.set("refreshMenuManagement", false)
                }
            }

            MenuManagementScreen(
                viewModel = menuManagementViewModel,
                onBack = { navController.popBackStack() },
                onNavigateToAddMenu = { navController.navigate(NavRoute.AddMenu.route) },
                onNavigateToMenuItems = { navController.navigate(NavRoute.MenuItems.route) },
                onNavigateToManageCategories = { navController.navigate(NavRoute.ManageCategories.route) }
            )
        }

        composable(NavRoute.ManageCategories.route) {
            val menuManagementViewModel: MenuManagementViewModel = hiltViewModel()
            val needsRefresh by (navController.currentBackStackEntry?.savedStateHandle
                ?.getStateFlow("refreshCategories", false)
                ?: MutableStateFlow(false))
                .collectAsState(initial = false)

            LaunchedEffect(needsRefresh) {
                if (needsRefresh) {
                    menuManagementViewModel.loadCategories()
                    navController.currentBackStackEntry?.savedStateHandle?.set("refreshCategories", false)
                }
            }

            ManageCategoriesScreen(
                viewModel = menuManagementViewModel,
                onBack = {
                    navController.previousBackStackEntry?.savedStateHandle?.set("refreshMenuManagement", true)
                    navController.popBackStack()
                },
                onNavigateToAddCategory = { navController.navigate(NavRoute.AddCategory.route) },
                onNavigateToEditCategory = { categoryId ->
                    navController.navigate(NavRoute.EditCategory.createRoute(categoryId))
                }
            )
        }

        composable(NavRoute.AddCategory.route) {
            AddCategoryScreen(
                onBack = { navController.popBackStack() },
                onCategoryAdded = {
                    navController.previousBackStackEntry?.savedStateHandle?.set("refreshCategories", true)
                    navController.popBackStack()
                }
            )
        }

        composable(NavRoute.AddMenu.route) {
            AddMenuScreen(
                onBack = { navController.popBackStack() },
                onMenuAdded = {
                    // Trigger refresh on whichever screen we return to
                    navController.previousBackStackEntry?.savedStateHandle?.set("refreshMenu", true)
                    navController.previousBackStackEntry?.savedStateHandle?.set("refreshMenuManagement", true)
                    navController.popBackStack()
                }
            )
        }

        composable(NavRoute.MenuItems.route) {
            val menuItemsViewModel: MenuItemsViewModel = hiltViewModel()

            val needsRefresh by (navController.currentBackStackEntry?.savedStateHandle
                ?.getStateFlow("refreshMenuItems", false)
                ?: MutableStateFlow(false))
                .collectAsState(initial = false)
            val uiState by menuItemsViewModel.uiState.collectAsState()

            LaunchedEffect(needsRefresh) {
                if (needsRefresh) {
                    menuItemsViewModel.loadMenuItems(
                        categoryId = uiState.selectedCategoryId,
                        isAvailable = uiState.availabilityFilter,
                        search = uiState.searchQuery.takeIf { it.isNotBlank() },
                        page = 1,
                        limit = uiState.limit
                    )
                    navController.currentBackStackEntry?.savedStateHandle?.set("refreshMenuItems", false)
                }
            }

            MenuItemsScreen(
                viewModel = menuItemsViewModel,
                onBack = {
                    navController.previousBackStackEntry?.savedStateHandle?.set("refreshMenuManagement", true)
                    navController.popBackStack()
                },
                onNavigateToAddMenuItem = { navController.navigate(NavRoute.AddMenuItem.route) },
                onNavigateToEditMenuItem = { menuItemId ->
                    navController.navigate(NavRoute.EditMenuItem.createRoute(menuItemId))
                }
            )
        }

        composable(NavRoute.AddMenuItem.route) { backStackEntry ->
            val parentEntry = navController.getBackStackEntry(NavRoute.MenuItems.route)
            val sharedViewModel: MenuItemsViewModel = hiltViewModel(parentEntry)

            AddMenuItemScreen(
                viewModel = sharedViewModel,
                onNavigateBack = {
                    navController.previousBackStackEntry?.savedStateHandle?.set("refreshMenuItems", true)
                    navController.popBackStack(NavRoute.MenuItems.route, inclusive = false)
                }
            )
        }

        composable(
            route = NavRoute.EditMenuItem.route,
            arguments = listOf(navArgument("menuItemId") { type = NavType.LongType })
        ) { backStackEntry ->
            val parentEntry = navController.getBackStackEntry(NavRoute.MenuItems.route)
            val sharedViewModel: MenuItemsViewModel = hiltViewModel(parentEntry)
            val menuItemId = backStackEntry.arguments?.getLong("menuItemId") ?: 0L

            EditMenuItemScreen(
                viewModel = sharedViewModel,
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

        composable(NavRoute.AllStaffManagement.route) {
            AllStaffManagementScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEditStaff = { staffId, storeId ->
                    navController.navigate(NavRoute.EditStaff.createRoute(staffId, storeId))
                }
            )
        }

        composable(NavRoute.SaleList.route) {
            SaleListScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(NavRoute.Referral.route) {
            com.swadratna.swadratna_admin.ui.referral.ReferralScreen()
        }
    }
}
