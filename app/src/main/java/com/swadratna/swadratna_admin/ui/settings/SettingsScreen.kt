package com.swadratna.swadratna_admin.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.swadratna.swadratna_admin.R
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToCampaigns: () -> Unit = {},
    onNavigateToStore: () -> Unit = {},
    onNavigateToAnalytics: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    var isEditMode by remember { mutableStateOf(false) }
    var userName by remember { mutableStateOf("Alice Johnson") }
    var userEmail by remember { mutableStateOf("alice.johnson@franchise.com") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(
                WindowInsets.safeDrawing.only(
                    WindowInsetsSides.Top + WindowInsetsSides.Bottom
                )
            )
    ) {
        TopAppBar(
            title = { Text("Settings") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                if (isEditMode) {
                    TextButton(onClick = {
                        isEditMode = false
                    }) {
                        Text("Save")
                    }
                }
            },
            windowInsets = WindowInsets(0.dp)
        )
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        .clickable(enabled = isEditMode) {
                            // Handle profile picture change
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isEditMode) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Change Profile Picture",
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (isEditMode) {
                    OutlinedTextField(
                        value = userName,
                        onValueChange = { userName = it },
                        label = { Text("Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(0.8f)
                    )
                } else {
                    Text(
                        text = userName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = userEmail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Account",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                SettingsItem(
                    icon = R.drawable.ic_edit,
                    title = "Edit Profile",
                    iconTint = MaterialTheme.colorScheme.primary,
                    onClick = { isEditMode = !isEditMode }
                )
                
                SettingsItem(
                    icon = R.drawable.ic_campaign,
                    title = "Manage Campaigns",
                    iconTint = MaterialTheme.colorScheme.primary,
                    onClick = onNavigateToCampaigns
                )
                
                SettingsItem(
                    icon = R.drawable.ic_store,
                    title = "Manage Store",
                    iconTint = MaterialTheme.colorScheme.primary,
                    onClick = onNavigateToStore
                )
                
                SettingsItem(
                    icon = R.drawable.ic_analytics,
                    title = "View Analytics",
                    iconTint = MaterialTheme.colorScheme.primary,
                    onClick = onNavigateToAnalytics
                )
                
                SettingsItem(
                    icon = R.drawable.ic_logout,
                    title = "Logout",
                    iconTint = Color.Red,
                    onClick = onLogout
                )
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun SettingsItem(
    icon: Int,
    title: String,
    iconTint: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
    if (title != "Logout") { // No divider after the last item
        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
    }
}