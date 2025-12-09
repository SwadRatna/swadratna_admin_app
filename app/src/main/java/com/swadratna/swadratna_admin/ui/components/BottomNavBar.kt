package com.swadratna.swadratna_admin.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.swadratna.swadratna_admin.R

data class BottomNavItem(
    val route: String,
    val iconRes: Int,
    val label: String
)

val bottomNavItems = listOf(
    BottomNavItem("dashboard", R.drawable.ic_home, "Home"),
    BottomNavItem("campaigns", R.drawable.ic_campaign, "Campaigns"),
    BottomNavItem("store", R.drawable.ic_store, "Store"),
    BottomNavItem("analytics", R.drawable.ic_analytics, "Info"),
    BottomNavItem("referral", R.drawable.ic_people, "Referral")
)

@Composable
fun BottomNavBar(
    navController: NavHostController,
    currentDestination: NavDestination?,
    modifier: Modifier = Modifier
) {
    NavigationBar(modifier = modifier) {
        bottomNavItems.forEach { item ->
            val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true

            NavigationBarItem(
                icon = {
                    Icon(
                        painterResource(id = item.iconRes),
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label) },
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}