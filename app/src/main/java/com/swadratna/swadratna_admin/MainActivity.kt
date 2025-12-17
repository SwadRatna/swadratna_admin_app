package com.swadratna.swadratna_admin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.swadratna.swadratna_admin.navigation.NavGraph
import com.swadratna.swadratna_admin.ui.components.BottomNavBar
import com.swadratna.swadratna_admin.ui.theme.SwadRatna_AdminTheme
import com.swadratna.swadratna_admin.utils.ConnectivityObserver
import com.swadratna.swadratna_admin.utils.NetworkConnectivityObserver
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private lateinit var connectivityObserver: ConnectivityObserver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        connectivityObserver = NetworkConnectivityObserver(applicationContext)

        enableEdgeToEdge()
        setContent {
            SwadRatna_AdminTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    
                    val mainRoutes = listOf("dashboard", "campaigns", "store", "analytics", "referral")
                    val currentRoute = navBackStackEntry?.destination?.route
                    
                    val showBottomBar = currentRoute in mainRoutes && currentRoute != "login"

                    val snackbarHostState = remember { SnackbarHostState() }
                    val status by connectivityObserver.observe().collectAsState(initial = ConnectivityObserver.Status.Available)

                    LaunchedEffect(status) {
                        if (status == ConnectivityObserver.Status.Lost || status == ConnectivityObserver.Status.Unavailable) {
                            snackbarHostState.showSnackbar(
                                message = "No internet connection",
                                duration = SnackbarDuration.Indefinite,
                            )
                        }
                    }
                    
                    Scaffold(
                        contentWindowInsets = WindowInsets(0.dp),
                        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                        bottomBar = {
                            if (showBottomBar) {
                                BottomNavBar(
                                    navController = navController,
                                    currentDestination = navBackStackEntry?.destination
                                )
                            }
                        }
                    ) { innerPadding ->
                        NavGraph(
                            navController = navController,
                            modifier = Modifier
                                .consumeWindowInsets(innerPadding)
                                .padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}