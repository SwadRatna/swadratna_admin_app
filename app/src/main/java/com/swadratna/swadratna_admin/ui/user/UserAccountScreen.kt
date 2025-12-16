package com.swadratna.swadratna_admin.ui.user

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.swadratna.swadratna_admin.ui.components.AppSearchField
import androidx.compose.material.icons.filled.Delete

data class User(
    val id: String,
    val name: String,
    val email: String,
    val isBlocked: Boolean,
    val isDeleted: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserAccountScreen(
    onBackClick: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    
    // Static list of users
    val initialUsers = remember {
        listOf(
            User("1", "John Doe", "john.doe@example.com", isBlocked = false, isDeleted = false),
            User("2", "Jane Smith", "jane.smith@example.com", isBlocked = true, isDeleted = false),
            User("3", "Alice Johnson", "alice.j@example.com", isBlocked = false, isDeleted = true),
            User("4", "Bob Brown", "bob.brown@test.com", isBlocked = false, isDeleted = false),
            User("5", "Charlie Davis", "charlie.d@example.com", isBlocked = true, isDeleted = true)
        )
    }

    var users by remember { mutableStateOf(initialUsers) }

    val filteredUsers = users.filter { 
        it.email.contains(searchQuery, ignoreCase = true) || 
        it.name.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("User Accounts") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            AppSearchField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = "Search by email or name...",
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredUsers) { user ->
                    UserCard(
                        user = user,
                        onBlockToggle = { isBlocked ->
                            users = users.map { 
                                if (it.id == user.id) it.copy(isBlocked = isBlocked) else it 
                            }
                        },
                        onDeleteClick = {
                            users = users.map { 
                                if (it.id == user.id) it.copy(isDeleted = true) else it 
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun UserCard(
    user: User,
    onBlockToggle: (Boolean) -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                
                // Delete Button
                if (!user.isDeleted) {
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete User",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                } else {
                     Text(
                        text = "Deleted",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Block Toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (user.isBlocked) "Blocked" else "Active",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (user.isBlocked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
                Switch(
                    checked = user.isBlocked,
                    onCheckedChange = onBlockToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = MaterialTheme.colorScheme.error,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
        }
    }
}
