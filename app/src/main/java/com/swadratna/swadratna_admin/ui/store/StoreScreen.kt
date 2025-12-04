package com.swadratna.swadratna_admin.ui.store

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.swadratna.swadratna_admin.R
import com.swadratna.swadratna_admin.ui.components.AppSearchField
import com.swadratna.swadratna_admin.ui.components.EmptyStateMessage
import com.swadratna.swadratna_admin.ui.components.LoadingIndicator
import com.swadratna.swadratna_admin.ui.store.components.StoreItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreScreen(
    onNavigateToCreateStore: () -> Unit,
    onNavigateToManageStore: (String) -> Unit,
    onNavigateToEditStore: (String) -> Unit,
    viewModel: StoreViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(key1 = true) {
        viewModel.onEvent(StoreEvent.RefreshStores)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Stores") },
                actions = {
                    IconButton(onClick = { viewModel.onEvent(StoreEvent.RefreshStores) }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreateStore,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Store",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            AppSearchField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onEvent(StoreEvent.SearchQueryChanged(it)) },
                placeholder = "Search stores...",
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = { viewModel.onEvent(StoreEvent.ToggleFilterMenu) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(painter = painterResource(R.drawable.ic_filter), contentDescription = "Filter")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Filter")
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                OutlinedButton(
                    onClick = { viewModel.onEvent(StoreEvent.ToggleSortMenu) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(painter = painterResource(R.drawable.ic_sort), contentDescription = "Sort")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Sort")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    uiState.isLoading -> {
                        LoadingIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    uiState.filteredStores.isEmpty() -> {
                        EmptyStateMessage(
                            message = "No stores found",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(vertical = 16.dp)
                        ) {
                            items(uiState.filteredStores) { store ->
                                StoreItem(
                                    store = store,
                                    onManage = onNavigateToManageStore,
                                    onEdit = { 
                                        viewModel.onEvent(StoreEvent.EditStore(it.toIntOrNull() ?: 0))
                                        onNavigateToEditStore(it)
                                    },
                                    onDelete = { storeId ->
                                        viewModel.onEvent(StoreEvent.DeleteStore(storeId.toIntOrNull() ?: 0))
                                    }
                                )
                            }
                        }
                    }
                }
                
                if (uiState.isFilterMenuVisible) {
                    StoreFilterMenu(
                        selectedStatus = uiState.filterStatus,
                        onStatusSelected = { viewModel.onEvent(StoreEvent.FilterByStatus(it)) },
                        onDismiss = { viewModel.onEvent(StoreEvent.ToggleFilterMenu) },
                        modifier = Modifier.align(Alignment.TopEnd)
                    )
                }
                
                if (uiState.isSortMenuVisible) {
                    StoreSortMenu(
                        selectedSortOption = uiState.sortOption,
                        onSortOptionSelected = { viewModel.onEvent(StoreEvent.SortBy(it)) },
                        onDismiss = { viewModel.onEvent(StoreEvent.ToggleSortMenu) },
                        modifier = Modifier.align(Alignment.TopEnd)
                    )
                }
            }
        }
    }
}

@Composable
fun StoreFilterMenu(
    selectedStatus: String?,
    onStatusSelected: (String?) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 3.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Filter by Status",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            StoreFilterOption(
                text = "All",
                isSelected = selectedStatus == null,
                onClick = { onStatusSelected(null); onDismiss() }
            )
            
            StoreFilterOption(
                text = "Active",
                isSelected = selectedStatus == "ACTIVE",
                onClick = { onStatusSelected("ACTIVE"); onDismiss() }
            )
            
            StoreFilterOption(
                text = "Inactive",
                isSelected = selectedStatus == "INACTIVE",
                onClick = { onStatusSelected("INACTIVE"); onDismiss() }
            )
            
            StoreFilterOption(
                text = "Pending",
                isSelected = selectedStatus == "PENDING",
                onClick = { onStatusSelected("PENDING"); onDismiss() }
            )
        }
    }
}

@Composable
fun StoreSortMenu(
    selectedSortOption: String,
    onSortOptionSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 3.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Sort by",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            StoreFilterOption(
                text = "Name (A-Z)",
                isSelected = selectedSortOption == "NAME_ASC",
                onClick = { onSortOptionSelected("NAME_ASC"); onDismiss() }
            )
            
            StoreFilterOption(
                text = "Name (Z-A)",
                isSelected = selectedSortOption == "NAME_DESC",
                onClick = { onSortOptionSelected("NAME_DESC"); onDismiss() }
            )
            
            StoreFilterOption(
                text = "Date (Newest first)",
                isSelected = selectedSortOption == "DATE_DESC",
                onClick = { onSortOptionSelected("DATE_DESC"); onDismiss() }
            )
            
            StoreFilterOption(
                text = "Date (Oldest first)",
                isSelected = selectedSortOption == "DATE_ASC",
                onClick = { onSortOptionSelected("DATE_ASC"); onDismiss() }
            )
        }
    }
}

@Composable
fun StoreFilterOption(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}