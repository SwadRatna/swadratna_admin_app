package com.swadratna.swadratna_admin.ui.menu

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import com.swadratna.swadratna_admin.R
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.swadratna.swadratna_admin.data.model.MenuCategory
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import androidx.compose.foundation.background
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuManagementScreen(
    viewModel: MenuManagementViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onNavigateToAddMenu: () -> Unit,
    onNavigateToMenuItems: () -> Unit,
    onNavigateToManageCategories: () -> Unit
) {
    val categoriesState by viewModel.categoriesState.collectAsState()
    val menuItemsState by viewModel.menuItemsState.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val isNextPageLoading by viewModel.isNextPageLoading.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
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
                    IconButton(onClick = {
                        viewModel.loadCategories()
                        viewModel.loadMenuItems(selectedCategory?.id)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh"
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
            // Management Buttons Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigateToManageCategories,
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_category),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Manage Categories",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                
                OutlinedButton(
                    onClick = onNavigateToMenuItems,
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_menu),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Manage Items",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.searchMenuItems(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                placeholder = { Text("Search menu items...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                singleLine = true
            )
            
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
            Text(
                text = selectedCategory?.let { "Menu Items in ${it.name}" } ?: "All Menu Items",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))

            val currentMenuState = menuItemsState
            
            val listState = androidx.compose.foundation.lazy.rememberLazyListState()
            
            // Infinite scroll logic
            LaunchedEffect(listState, currentMenuState) {
                androidx.compose.runtime.snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
                    .collect { index ->
                        val itemsCount = (currentMenuState as? MenuUiState.Success)?.items?.size ?: 0
                        if (index != null && itemsCount > 0 && index >= (itemsCount - 2)) {
                            viewModel.loadNextPage()
                        }
                    }
            }

            when (currentMenuState) {
                is MenuUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is MenuUiState.Success -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(currentMenuState.items) { item ->
                            MenuItemCard(
                                item = item,
                                onToggleAvailability = { viewModel.toggleAvailability(item) }
                            )
                        }
                        if (isNextPageLoading) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                }
                            }
                        }
                    }
                }
                is MenuUiState.Error -> {
                    Box(modifier = Modifier.weight(1f)) {
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
                Row(modifier = Modifier.weight(1f)) {
                    if (item.image != null && item.image!!.isNotBlank()) {
                        AsyncImage(
                            model = item.image,
                            contentDescription = "Menu Item Image",
                            modifier = Modifier
                                .size(96.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outline,
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outline,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.fillMaxWidth()) {
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
                }
                Switch(
                    checked = item.isAvailable,
                    onCheckedChange = { onToggleAvailability() }
                )
            }
        }
    }
}