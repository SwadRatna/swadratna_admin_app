package com.swadratna.swadratna_admin.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.swadratna.swadratna_admin.ui.dashboard.DashboardScreen
import com.swadratna.swadratna_admin.ui.home.HomeScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = NavRoute.Dashboard.route,
    modifier: Modifier = Modifier
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
            // TODO: Add Campaigns screen
            HomeScreen()
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
    object Store : NavRoute("store")
    object Analytics : NavRoute("analytics")
}