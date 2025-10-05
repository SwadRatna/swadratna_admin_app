package com.swadratna.swadratna_admin.ui.menu

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.swadratna.swadratna_admin.data.model.MenuCategory
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuManagementScreen(
    viewModel: MenuManagementViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onNavigateToAddCategory: () -> Unit,
    onNavigateToAddMenu: () -> Unit,
    onNavigateToMenuItems: () -> Unit
) {
    val categoriesState by viewModel.categoriesState.collectAsState()
    val menuItemsState by viewModel.menuItemsState.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val context = LocalContext.current
    
    // Handle success messages with Toast
    LaunchedEffect(menuItemsState) {
        val currentState = menuItemsState
        if (currentState is MenuUiState.Success && currentState.successMessage != null) {
            Toast.makeText(context, currentState.successMessage, Toast.LENGTH_SHORT).show()
            viewModel.clearSuccessMessage()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Menu Management") },
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
            // Categories Section
            Text(
                text = "Categories",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            when (categoriesState) {
                is MenuCategoriesUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is MenuCategoriesUiState.Success -> {
                    val successState = categoriesState as MenuCategoriesUiState.Success
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        item {
                            CategoryChip(
                                category = null,
                                isSelected = selectedCategory == null,
                                onClick = { viewModel.selectCategory(null) }
                            )
                        }
                        items(successState.categories) { category ->
                            CategoryChip(
                                category = category,
                                isSelected = selectedCategory?.id == category.id,
                                onClick = { viewModel.selectCategory(category) }
                            )
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

            Spacer(modifier = Modifier.height(24.dp))

            // Menu Items Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedCategory?.let { "Menu Items in ${it.name}" } ?: "All Menu Items",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                
                OutlinedButton(
                    onClick = onNavigateToMenuItems,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text("Manage Items")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))

            val currentMenuState = menuItemsState
            when (currentMenuState) {
                is MenuUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is MenuUiState.Success -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(currentMenuState.items) { item ->
                            MenuItemCard(
                                item = item,
                                onToggleAvailability = { viewModel.toggleAvailability(item) }
                            )
                        }
                    }
                }
                is MenuUiState.Error -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = currentMenuState.message,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryChip(
    category: MenuCategory?,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        onClick = onClick,
        label = {
            Text(
                text = category?.name ?: "All",
                style = MaterialTheme.typography.labelMedium
            )
        },
        selected = isSelected,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

@Composable
private fun MenuItemCard(
    item: com.swadratna.swadratna_admin.data.model.MenuItem,
    onToggleAvailability: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    if (item.categoryName != null) {
                        Text(
                            text = "Category: ${item.categoryName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    Text(
                        text = "$${item.price}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                Switch(
                    checked = item.isAvailable,
                    onCheckedChange = { onToggleAvailability() }
                )
            }
        }
    }
}