package com.swadratna.swadratna_admin.presentation.screens.menu

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.swadratna.swadratna_admin.presentation.viewmodels.StoreLocationMenuViewModel
import com.swadratna.swadratna_admin.data.model.MenuItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreLocationMenuScreen(
    storeId: String,
    onBack: () -> Unit,
    viewModel: StoreLocationMenuViewModel = hiltViewModel()
){
    LaunchedEffect(storeId) {
        viewModel.setLocation(storeId)
        viewModel.load()
    }
    val uiState by viewModel.uiState.collectAsState()
    var search by remember(uiState.search) { mutableStateOf(uiState.search) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Manage Menu") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OutlinedTextField(
                value = search,
                onValueChange = {
                    search = it
                    viewModel.setSearch(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search menu items...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                singleLine = true
            )

            if (uiState.isLoading && uiState.menu.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.menu) { item ->
                        StoreLocationMenuCard(
                            item = item,
                            onEdit = { viewModel.openEdit(item) }
                        )
                    }
                }
            }
        }
    }

    val editingItem = uiState.editingItem
    if (editingItem != null) {
        EditLocationMenuDialog(
            item = editingItem,
            onDismiss = { viewModel.dismissEdit() },
            onSave = { price, available, reason ->
                viewModel.saveEdit(price, available, reason)
            },
            isSaving = uiState.isSaving
        )
    }
}

@Composable
private fun StoreLocationMenuCard(
    item: MenuItem,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                if (!item.description.isNullOrBlank()) {
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (!item.categoryName.isNullOrBlank()) {
                    Text(
                        text = item.categoryName ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit for location")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditLocationMenuDialog(
    item: MenuItem,
    onDismiss: () -> Unit,
    onSave: (Double?, Boolean, String?) -> Unit,
    isSaving: Boolean
) {
    var priceText by remember { mutableStateOf("") }
    var available by remember { mutableStateOf(item.isAvailable) }
    var reason by remember { mutableStateOf(item.unavailableReason ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit for Location") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("Location Price") },
                    singleLine = true
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Available")
                    Switch(checked = available, onCheckedChange = { available = it })
                }
                if (!available) {
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Unavailable Reason") }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val price = priceText.toDoubleOrNull()
                    onSave(price, available, if (available) null else reason.ifBlank { null })
                },
                enabled = !isSaving
            ) {
                Text(if (isSaving) "Saving..." else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) {
                Text("Cancel")
            }
        }
    )
}
