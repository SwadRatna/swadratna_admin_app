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
            DashboardScreen()
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
            // TODO: Add Store screen
            HomeScreen()
        }
        composable(NavRoute.Analytics.route) {
            // TODO: Add Analytics screen
            HomeScreen()
        }
    }
}

sealed class NavRoute(val route: String) {
    object Dashboard : NavRoute("dashboard")
    object Home : NavRoute("home")
    object Campaigns : NavRoute("campaigns")
    object CreateCampaign : NavRoute("create_campaign")
    object Store : NavRoute("store")
    object Analytics : NavRoute("analytics")
}