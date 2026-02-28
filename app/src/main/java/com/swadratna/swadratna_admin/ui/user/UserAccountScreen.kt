package com.swadratna.swadratna_admin.ui.user

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.swadratna.swadratna_admin.ui.components.AppSearchField
import androidx.compose.material.icons.filled.Delete
import com.swadratna.swadratna_admin.data.remote.dto.CustomerDto
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserAccountScreen(
    onBackClick: () -> Unit,
    viewModel: UserAccountViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) { viewModel.load(reset = true) }
    var searchQuery by remember(uiState.search) { mutableStateOf(uiState.search) }
    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker by remember { mutableStateOf(false) }
    val fromPickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
    val toPickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
    val formatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }
    fun millisToDateString(millis: Long?): String? {
        if (millis == null) return null
        val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
        return date.format(formatter)
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
                onValueChange = {
                    searchQuery = it
                    viewModel.search(it)
                },
                placeholder = "Search by email/phone/name",
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Select the date \"from\" to \"to\" to get users and gain/loss.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { showFromPicker = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(uiState.fromDate ?: "From date")
                        }
                        OutlinedButton(
                            onClick = { showToPicker = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(uiState.toDate ?: "To date")
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    val growth = uiState.growth
                    val dateRange = uiState.dateRangeFromServer
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Selected Range", style = MaterialTheme.typography.labelMedium)
                            Text(
                                text = "${uiState.fromDate ?: dateRange?.from_date ?: "-"} \u2192 ${uiState.toDate ?: dateRange?.to_date ?: "-"}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Current Count", style = MaterialTheme.typography.labelMedium)
                            Text(
                                text = (growth?.current_period_count ?: 0).toString(),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Previous Range", style = MaterialTheme.typography.labelMedium)
                            val prev = growth?.previous_period
                            Text(
                                text = "${prev?.from_date ?: "-"} \u2192 ${prev?.to_date ?: "-"}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Previous Count", style = MaterialTheme.typography.labelMedium)
                            Text(
                                text = (growth?.previous_period_count ?: 0).toString(),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(12.dp))
                    val pct = growth?.growth_percentage ?: 0.0
                    val gainLabel = if (pct >= 0.0) "Gain" else "Loss"
                    val gainColor = if (pct >= 0.0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(gainLabel, style = MaterialTheme.typography.labelLarge, color = gainColor)
                        Text(
                            text = String.format("%.1f%%", pct),
                            style = MaterialTheme.typography.headlineMedium,
                            color = gainColor
                        )
                    }
                }
            }
            if (showFromPicker) {
                DatePickerDialog(
                    onDismissRequest = { showFromPicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            val sel = millisToDateString(fromPickerState.selectedDateMillis)
                            viewModel.setFromDate(sel)
                            showFromPicker = false
                        }) { Text("OK") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showFromPicker = false }) { Text("Cancel") }
                    }
                ) {
                    DatePicker(state = fromPickerState)
                }
            }
            if (showToPicker) {
                DatePickerDialog(
                    onDismissRequest = { showToPicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            val sel = millisToDateString(toPickerState.selectedDateMillis)
                            viewModel.setToDate(sel)
                            showToPicker = false
                        }) { Text("OK") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showToPicker = false }) { Text("Cancel") }
                    }
                ) {
                    DatePicker(state = toPickerState)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(uiState.customers) { user ->
                    UserCard(
                        user = user,
                        onBlockToggle = { isBlocked -> viewModel.toggleBlock(user, isBlocked) },
                        onDeleteClick = { viewModel.delete(user) }
                    )
                }
                if (!uiState.isLoading && uiState.customers.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("No users found", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                if (uiState.hasNext) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { viewModel.loadNextPage() },
                                enabled = !uiState.isLoading,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Load more")
                            }
                            Button(
                                onClick = { viewModel.loadAll() },
                                enabled = !uiState.isLoading,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Load all")
                            }
                        }
                    }
                }
            }
            uiState.error?.let { err ->
                Spacer(Modifier.height(8.dp))
                Text(
                    text = err,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            if (uiState.isLoading) {
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
fun UserCard(
    user: CustomerDto,
    onBlockToggle: (Boolean) -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
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
                        text = user.name ?: "",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = user.email ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = user.mobile_number ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                
                // Delete Button
                if (user.deleted != true) {
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
                    text = if (user.blocked == true || user.status == "blocked") "Blocked" else "Active",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (user.blocked == true || user.status == "blocked") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
                Switch(
                    checked = user.blocked == true || user.status == "blocked",
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
