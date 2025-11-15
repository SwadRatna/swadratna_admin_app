package com.swadratna.swadratna_admin.presentation.screens.menu

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.swadratna.swadratna_admin.data.model.MenuCategory
import com.swadratna.swadratna_admin.ui.menu.MenuManagementViewModel
import com.swadratna.swadratna_admin.ui.menu.MenuCategoriesUiState
import com.swadratna.swadratna_admin.ui.menu.MenuUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageCategoriesScreen(
    onBack: () -> Unit,
    onNavigateToAddCategory: () -> Unit,
    onNavigateToEditCategory: (Long) -> Unit,
    viewModel: MenuManagementViewModel = hiltViewModel()
) {
    val categoriesState by viewModel.categoriesState.collectAsState()
    val menuItemsState by viewModel.menuItemsState.collectAsState()
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    var categoryToDelete by remember { mutableStateOf<MenuCategory?>(null) }

    // Handle success messages with Toast
    LaunchedEffect(categoriesState) {
        val currentState = categoriesState
        if (currentState is MenuCategoriesUiState.Success && currentState.successMessage != null) {
            Toast.makeText(context, currentState.successMessage, Toast.LENGTH_SHORT).show()
            viewModel.clearCategorySuccessMessage()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Manage Categories") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToAddCategory) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Category"
                        )
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
            when (categoriesState) {
                is MenuCategoriesUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is MenuCategoriesUiState.Success -> {
                    val successState = categoriesState as MenuCategoriesUiState.Success
                    if (successState.categories.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No categories found",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Add your first category to get started",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(successState.categories) { category ->
                                CategoryCard(
                                    category = category,
                                    onDeleteClick = {
                                        categoryToDelete = category
                                        // Prefetch items for this category to show in the dialog
                                        category.id?.let { viewModel.loadMenuItems(it) }
                                        showDeleteDialog = true
                                    },
                                    onToggleAvailability = { viewModel.toggleCategoryAvailability(category) },
                                    onEditClick = {
                                        category.id?.let { onNavigateToEditCategory(it.toLong()) }
                                    }
                                )
                            }
                        }
                    }
                }
                is MenuCategoriesUiState.Error -> {
                    val errorState = categoriesState as MenuCategoriesUiState.Error
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = errorState.message,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog && categoryToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                categoryToDelete = null
            },
            title = { Text("Delete Category") },
            text = {
                Column {
                    Text(
                        text = "Are you sure you want to delete \"${categoryToDelete!!.name}\"? This action cannot be undone.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    when (menuItemsState) {
                        is MenuUiState.Loading -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Checking associated items...")
                            }
                        }
                        is MenuUiState.Error -> {
                            val err = menuItemsState as MenuUiState.Error
                            Text(
                                text = err.message,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        is MenuUiState.Success -> {
                            val items = (menuItemsState as MenuUiState.Success).items
                            if (items.isNotEmpty()) {
                                Text(
                                    text = "The following item(s) will also be deleted:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                // Show up to 5 item names
                                val preview = items.take(5)
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    preview.forEach { item ->
                                        Text("• ${item.name}", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                                if (items.size > 5) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "and ${items.size - 5} more...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        categoryToDelete?.let { category ->
                            // Perform cascade delete: delete associated items first, then category
                            viewModel.deleteCategoryCascade(category)
                        }
                        showDeleteDialog = false
                        categoryToDelete = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        categoryToDelete = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun CategoryCard(
    category: MenuCategory,
    onDeleteClick: () -> Unit,
    onToggleAvailability: () -> Unit,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = category.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Display Order: ${category.displayOrder}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = if (category.isActive) "Active" else "Inactive",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (category.isActive) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.error
                    )
                }
            }
            
            Column(
                horizontalAlignment = Alignment.End
            ) {
                // Toggle availability switch
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Available",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Switch(
                        checked = category.isActive,
                        onCheckedChange = { onToggleAvailability() }
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Delete button (top)
                IconButton(
                    onClick = onDeleteClick
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Category",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
                
                // Edit button (bottom)
                IconButton(
                    onClick = onEditClick
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Category"
                    )
                }
            }
        }
    }
}