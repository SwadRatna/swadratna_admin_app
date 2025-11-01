package com.swadratna.swadratna_admin.ui.staff

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.swadratna.swadratna_admin.R
import com.swadratna.swadratna_admin.data.model.Staff
import com.swadratna.swadratna_admin.data.model.StaffStatus
import com.swadratna.swadratna_admin.ui.components.AppSearchField
import com.swadratna.swadratna_admin.ui.store.StoreEvent
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffManagementScreen(
    storeId: String,
    modifier: Modifier = Modifier,
    viewModel: StaffManagementViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToAddStaff: () -> Unit = {},
    onNavigateToEditStaff: (Int) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Load staff data when screen is first displayed
    LaunchedEffect(storeId) {
        if (storeId.isNotEmpty()) {
            viewModel.loadStaff(storeId.toInt())
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Staff Management") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { 
                            if (storeId.isNotEmpty()) {
                                viewModel.loadStaff(storeId.toInt())
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.Refresh, 
                            contentDescription = "Refresh"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddStaff,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Staff",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AppSearchField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = "Search staff...",
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = { viewModel.onEvent(StaffEvent.ToggleFilterMenu)  },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(painter = painterResource(R.drawable.ic_filter), contentDescription = "Filter")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Filter")
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                OutlinedButton(
                    onClick = {viewModel.onEvent(StaffEvent.ToggleSortMenu)  },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(painter = painterResource(R.drawable.ic_sort), contentDescription = "Sort")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Sort")
                }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Box(modifier = Modifier.fillMaxSize()) {
                when {
                    uiState.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    uiState.error != null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = uiState.error ?: "Unknown error",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { 
                                        if (storeId.isNotEmpty()) {
                                            viewModel.loadStaff(storeId.toInt())
                                        }
                                    }
                                ) {
                                    Text("Retry")
                                }
                            }
                        }
                    }
                    uiState.staffList.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_home),
                                    contentDescription = "No staff",
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No staff members found",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Add staff members to get started",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(uiState.staffList) { staff ->
                                StaffItem(
                                    staff = staff,
                                    onEdit = { staffId -> onNavigateToEditStaff(staffId) },
                                    onDelete = { staffId -> viewModel.deleteStaff(staffId) },
                                    snackbarHostState = snackbarHostState,
                                    password = staff.password ?: uiState.passwordsByStaffId[staff.id]
                                )
                            }
                        }
                    }
                }
                
                if (uiState.isFilterMenuVisible) {
                    FilterMenu(
                        selectedFilter = uiState.selectedFilter,
                        onFilterSelected = { viewModel.updateFilter(it) },
                        onDismiss = { viewModel.onEvent(StaffEvent.ToggleFilterMenu) },
                        modifier = Modifier.align(Alignment.TopEnd)
                    )
                }
                
                if (uiState.isSortMenuVisible) {
                    SortMenu(
                        selectedSortOrder = uiState.selectedSortOrder,
                        onSortOrderSelected = { viewModel.updateSortOrder(it) },
                        onDismiss = { viewModel.onEvent(StaffEvent.ToggleSortMenu) },
                        modifier = Modifier.align(Alignment.TopEnd)
                    )
                }
            }
        }
    }
}

@Composable
fun StaffItem(
    staff: Staff,
    onEdit: (Int) -> Unit,
    onDelete: (Int) -> Unit,
    snackbarHostState: SnackbarHostState,
    password: String?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_home),
                            contentDescription = "${staff.name}'s profile",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column {
                        Text(
                            text = staff.name ?: "Unknown Name",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Text(
                            text = staff.position ?: "Unknown Position",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
                
                StaffStatusChip(status = staff.status)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = run {
                    val startTime = staff.workingHours?.startTime ?: staff.shiftTiming?.startTime
                    val endTime = staff.workingHours?.endTime ?: staff.shiftTiming?.endTime
                    
                    if (startTime != null && endTime != null) {
                        "$startTime - $endTime"
                    } else {
                        "Working hours not set"
                    }
                },
                style = MaterialTheme.typography.bodyMedium,
                color = run {
                    val hasWorkingHours = (staff.workingHours?.startTime != null && staff.workingHours?.endTime != null) ||
                                         (staff.shiftTiming?.startTime != null && staff.shiftTiming?.endTime != null)
                    if (hasWorkingHours) 
                        MaterialTheme.colorScheme.onSurface 
                    else 
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
                val coroutineScope = rememberCoroutineScope()
                IconButton(onClick = {
                    val email = staff.email ?: ""
                    val pwd = password ?: ""
                    val text = "Email: $email" + if (pwd.isNotBlank()) "\nPassword: $pwd" else ""
                    clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(text))
                    coroutineScope.launch {
                        val msg = if (pwd.isBlank()) "Login details copied. Password may not be available." else "Login details copied"
                        snackbarHostState.showSnackbar(msg)
                    }
                }) {
                    Icon(painter = painterResource(R.drawable.ic_copy_content), contentDescription = "Copy login details")
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = { onEdit(staff.id) }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = { onDelete(staff.id) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                }
            }
        }
    }
}

@Composable
fun FilterMenu(
    selectedFilter: String?,
    onFilterSelected: (String?) -> Unit,
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
            
            FilterOption(
                text = "All",
                isSelected = selectedFilter == null,
                onClick = { onFilterSelected(null); onDismiss() }
            )
            
            FilterOption(
                text = "Active",
                isSelected = selectedFilter == "ACTIVE",
                onClick = { onFilterSelected("ACTIVE"); onDismiss() }
            )
            
            FilterOption(
                text = "On Leave",
                isSelected = selectedFilter == "ON_LEAVE",
                onClick = { onFilterSelected("ON_LEAVE"); onDismiss() }
            )
            
            FilterOption(
                text = "Terminated",
                isSelected = selectedFilter == "TERMINATED",
                onClick = { onFilterSelected("TERMINATED"); onDismiss() }
            )
        }
    }
}

@Composable
fun SortMenu(
    selectedSortOrder: String,
    onSortOrderSelected: (String) -> Unit,
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
            
            FilterOption(
                text = "Name (A-Z)",
                isSelected = selectedSortOrder == "NAME_ASC",
                onClick = { onSortOrderSelected("NAME_ASC"); onDismiss() }
            )
            
            FilterOption(
                text = "Name (Z-A)",
                isSelected = selectedSortOrder == "NAME_DESC",
                onClick = { onSortOrderSelected("NAME_DESC"); onDismiss() }
            )
            
            FilterOption(
                text = "Position (A-Z)",
                isSelected = selectedSortOrder == "POSITION_ASC",
                onClick = { onSortOrderSelected("POSITION_ASC"); onDismiss() }
            )
            
            FilterOption(
                text = "Position (Z-A)",
                isSelected = selectedSortOrder == "POSITION_DESC",
                onClick = { onSortOrderSelected("POSITION_DESC"); onDismiss() }
            )
        }
    }
}

@Composable
fun FilterOption(
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

@Composable
fun StaffStatusChip(status: StaffStatus) {
    val (backgroundColor, textColor) = when (status) {
        StaffStatus.ACTIVE -> Pair(Color(0xFF4CAF50).copy(alpha = 0.2f), Color(0xFF4CAF50))
        StaffStatus.INACTIVE -> Pair(Color(0xFFF44336).copy(alpha = 0.2f), Color(0xFFF44336))
        StaffStatus.ON_BREAK -> Pair(Color(0xFFFF9800).copy(alpha = 0.2f), Color(0xFFFF9800))
    }
    
    val statusText = when (status) {
        StaffStatus.ACTIVE -> "Active"
        StaffStatus.INACTIVE -> "Inactive"
        StaffStatus.ON_BREAK -> "On Break"
    }
    
    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.padding(4.dp)
    ) {
        Text(
            text = statusText,
            color = textColor,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}