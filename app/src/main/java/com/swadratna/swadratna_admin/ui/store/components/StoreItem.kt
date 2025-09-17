package com.swadratna.swadratna_admin.ui.store.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.swadratna.swadratna_admin.data.model.Store
import com.swadratna.swadratna_admin.data.model.StoreStatus

@Composable
fun StoreItem(
    store: Store,
    onManage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = store.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Box {
                    var expanded by remember { mutableStateOf(false) }
                    
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More options")
                    }
                    
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = { expanded = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = { expanded = false }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Status chip
            StoreStatusChip(status = store.status)
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Location
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = store.location,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Address
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(modifier = Modifier.width(20.dp))
                Text(
                    text = store.address,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Creation date
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Created on: ${store.getFormattedCreationDate()}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Manage button
            TextButton(
                onClick = { onManage(store.id) },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Manage")
            }
        }
    }
}

@Composable
fun StoreStatusChip(status: StoreStatus) {
    val (backgroundColor, contentColor) = when (status) {
        StoreStatus.ACTIVE -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        StoreStatus.INACTIVE -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        StoreStatus.PENDING -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
    }
    
    val statusText = when (status) {
        StoreStatus.ACTIVE -> "Active"
        StoreStatus.INACTIVE -> "Inactive"
        StoreStatus.PENDING -> "Pending"
    }
    
    SuggestionChip(
        onClick = { },
        label = { Text(statusText) },
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = backgroundColor,
            labelColor = contentColor
        )
    )
}