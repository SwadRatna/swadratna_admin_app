package com.swadratna.swadratna_admin.ui.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.swadratna.swadratna_admin.data.model.Activity
import com.swadratna.swadratna_admin.data.model.ActivityType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    onNavigateBack: () -> Unit,
    viewModel: NotificationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState.activities.isNotEmpty()) {
                        TextButton(
                            onClick = { viewModel.clearAllActivities() }
                        ) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "Clear all",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Clear All")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (uiState.activities.isEmpty()) {
                item {
                    EmptyNotificationsState()
                }
            } else {
                items(uiState.activities) { activity ->
                    ActivityNotificationItem(activity = activity)
                }
            }
        }
    }
}

@Composable
fun EmptyNotificationsState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.Clear,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No notifications yet",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Activity notifications will appear here when you create, update, or delete stores and staff.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
fun ActivityNotificationItem(activity: Activity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Activity type icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(activity.type.getColor()).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getActivityIcon(activity.type),
                    contentDescription = null,
                    tint = Color(activity.type.getColor()),
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Activity content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = activity.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = activity.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = activity.getFormattedDateTime(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun getActivityIcon(type: ActivityType): ImageVector {
    return when (type) {
        ActivityType.STORE_CREATED -> Icons.Filled.LocationOn
        ActivityType.STORE_UPDATED -> Icons.Filled.Edit
        ActivityType.STORE_DELETED -> Icons.Filled.Delete
        ActivityType.STAFF_CREATED -> Icons.Filled.Add
        ActivityType.STAFF_UPDATED -> Icons.Filled.Person
        ActivityType.STAFF_DELETED -> Icons.Filled.Delete
        ActivityType.MENU_ITEM_CREATED -> Icons.Filled.Add
        ActivityType.MENU_ITEM_UPDATED -> Icons.Filled.Edit
        ActivityType.MENU_ITEM_DELETED -> Icons.Filled.Delete
        ActivityType.MENU_ITEM_AVAILABILITY_CHANGED -> Icons.Filled.Build
        ActivityType.CATEGORY_CREATED -> Icons.Filled.Search
        ActivityType.CATEGORY_UPDATED -> Icons.Filled.Edit
        ActivityType.CATEGORY_DELETED -> Icons.Filled.Delete
    }
}