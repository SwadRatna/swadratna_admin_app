package com.swadratna.swadratna_admin.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.swadratna.swadratna_admin.ui.dashboard.DashboardScreen
import com.swadratna.swadratna_admin.ui.home.HomeScreen
import com.swadratna.swadratna_admin.ui.campaign.CampaignScreen
import com.swadratna.swadratna_admin.ui.campaign.CreateCampaignScreen
import com.swadratna.swadratna_admin.ui.settings.SettingsScreen
import com.swadratna.swadratna_admin.ui.store.StoreScreen
import com.swadratna.swadratna_admin.ui.store.CreateStoreScreen

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
        composable(NavRoute.Store.route) {
            StoreScreen(
                onNavigateToCreateStore = {
                    navController.navigate(NavRoute.CreateStore.route)
                },
                onNavigateToManageStore = { storeId ->
                    // TODO: Navigate to store details when implemented
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
        composable(NavRoute.Analytics.route) {
            // TODO: Add Analytics screen
            HomeScreen()
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
    }
}

sealed class NavRoute(val route: String) {
    object Dashboard : NavRoute("dashboard")
    object Home : NavRoute("home")
    object Campaigns : NavRoute("campaigns")
    object CreateCampaign : NavRoute("create_campaign")
    object Store : NavRoute("store")
    object CreateStore : NavRoute("create_store")
    object Analytics : NavRoute("analytics")
    object Settings : NavRoute("settings")
}