package com.swadratna.swadratna_admin.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.layout.WindowInsets
import com.swadratna.swadratna_admin.R
import androidx.compose.ui.res.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel(),
    onNavigateToSettings: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToAllStaffManagement: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Panel") },
                actions = {
                    Row(
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        val showProfileIcon = false
                        if (showProfileIcon) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .align(Alignment.CenterVertically)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                    .clickable { onNavigateToSettings() }
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                        IconButton(onClick = onNavigateToNotifications) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                        }
                        IconButton(onClick = onNavigateToAllStaffManagement) {
                            Icon(painter = painterResource(R.drawable.ic_person), contentDescription = "All Staff")
                        }
                    }
                },
            )
        },
        contentWindowInsets = WindowInsets(0.dp)
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = uiState.error ?: "Error",
                        color = Color.Red
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = modifier
                        .fillMaxSize()
                        .consumeWindowInsets(paddingValues)
                        .padding(paddingValues),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { StatisticsSection(uiState) }
                    item { RecentActivitySection(uiState.recentActivities, onNavigateToNotifications) }
                    item { TopPerformingStoreSection(uiState.topStore) }
                }
            }
        }
    }
}



@Composable
fun StatisticsSection(uiState: DashboardUiState) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard(
                title = "Total Campaigns",
                value = uiState.totalCampaigns.toString(),
                change = uiState.campaignsChange,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Active Store",
                value = uiState.activeStore.toString(),
                change = uiState.storeChange,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard(
                title = "Top Seller",
                value = uiState.topSeller,
                change = uiState.topSellerMetric,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "New Users",
                value = uiState.newUsers.toString(),
                change = uiState.newUsersChange,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    change: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = if (title == "Top Seller") {
                    MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                },
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))

            val changeColor = when {
                change.trim().startsWith("-") -> MaterialTheme.colorScheme.error
                change.trim().firstOrNull()?.isDigit() == true || change.trim().startsWith("+") -> Color(0xFF4CAF50)
                else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            }

            Text(
                text = change,
                style = MaterialTheme.typography.bodySmall,
                color = changeColor
            )
        }
    }
}

@Composable
fun RecentActivitySection(activities: List<ActivityItem>, onNavigateToNotifications: () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "Recent Activity",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        if (activities.isEmpty()) {
            Text(
                text = "No activity yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            activities.forEach { activity ->
                ActivityItem(activity)
            }
            Text(
                text = "View All Activity",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .clickable { onNavigateToNotifications() }
            )
        }
    }
}

@Composable
fun ActivityItem(activity: ActivityItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = activity.title,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = activity.time,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun TopPerformingStoreSection(storeItem: List<StoreItem>) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "Top Performing Store",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (storeItem.size <= 1) {
            Text(
                text = "No comparison available",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            storeItem.forEachIndexed { index, store ->
                StoreItem(
                    store = store,
                    color = when (index) {
                        0 -> Color(0xFFE8F5E9)
                        1 -> Color(0xFFFFEBEE)
                        else -> Color(0xFFF3E5F5)
                    }
                )
            }
            Text(
                text = "View Full Leaderboard",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .clickable { /* TODO */ }
            )
        }
    }
}



@Composable
fun StoreItem(store: StoreItem, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = store.name,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = store.revenue,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
    }
}