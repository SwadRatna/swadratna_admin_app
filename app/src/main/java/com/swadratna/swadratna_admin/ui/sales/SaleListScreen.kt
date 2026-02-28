package com.swadratna.swadratna_admin.ui.sales

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.swadratna.swadratna_admin.data.model.SaleDto
import com.swadratna.swadratna_admin.data.model.Store
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaleListScreen(
    onNavigateBack: () -> Unit,
    viewModel: SalesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val apiDateFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }

    LaunchedEffect(Unit) {
        val today = java.util.Date()
        viewModel.fetchSales(date = apiDateFormatter.format(today))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Sale List",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = { }
            )
        },
        bottomBar = { }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // Filter Section
            FilterSection(
                stores = uiState.stores,
                onFilterChanged = { date, fromDate, toDate, locationId ->
                    val adjustedToDate = toDate?.let {
                        val c = java.util.Calendar.getInstance()
                        c.time = it
                        c.add(java.util.Calendar.DAY_OF_MONTH, 1)
                        c.time
                    }

                    viewModel.fetchSales(
                        date = date?.let { apiDateFormatter.format(it) },
                        fromDate = fromDate?.let { apiDateFormatter.format(it) },
                        toDate = adjustedToDate?.let { apiDateFormatter.format(it) },
                        locationIds = locationId
                    )
                }
            )

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.error != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: ${uiState.error}", color = Color.Red)
                }
            } else {
                SummarySection(
                    totalAmount = uiState.salesResponse?.summary?.totalAmount ?: 0.0,
                    totalCount = uiState.salesResponse?.summary?.count ?: 0
                )

                SaleList(
                    sales = uiState.salesResponse?.sales ?: emptyList()
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSection(
    stores: List<Store>,
    onFilterChanged: (Date?, Date?, Date?, String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("Today") }
    val filters = listOf("Today", "Yesterday", "This Week", "This Month", "Custom")

    var storeExpanded by remember { mutableStateOf(false) }
    
    val locations = remember(stores) {
        val list = mutableListOf<Pair<String, String?>>()
        list.add("All Locations" to null)
        list.addAll(stores.map { it.name to it.id.toString() })
        list
    }
    
    var selectedLocationName by remember { mutableStateOf("All Locations") }
    var selectedLocationId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(stores) {
        if (selectedLocationId == null && selectedLocationName == "All Locations") {
             val defaultStore = stores.find { it.id == 1000003 }
             if (defaultStore != null) {
                 selectedLocationName = defaultStore.name
                 selectedLocationId = defaultStore.id.toString()
             }
        }
    }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var startDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var endDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }

    val dateFormatter = remember { SimpleDateFormat("dd/MM/yy", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                OutlinedButton(
                    onClick = { expanded = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(selectedFilter, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
                        Icon(
                            painter = painterResource(android.R.drawable.arrow_down_float),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxWidth(0.5f)
                ) {
                    filters.forEach { filter ->
                        DropdownMenuItem(
                            text = { Text(filter) },
                            onClick = {
                                selectedFilter = filter
                                expanded = false
                                
                                val calendar = java.util.Calendar.getInstance()
                                val today = System.currentTimeMillis()
                                
                                when (filter) {
                                    "Today" -> {
                                        startDateMillis = today
                                        endDateMillis = today
                                        onFilterChanged(null, Date(startDateMillis), Date(endDateMillis), selectedLocationId)
                                    }
                                    "Yesterday" -> {
                                        calendar.add(java.util.Calendar.DAY_OF_YEAR, -1)
                                        startDateMillis = calendar.timeInMillis
                                        endDateMillis = calendar.timeInMillis
                                        onFilterChanged(null, Date(startDateMillis), Date(endDateMillis), selectedLocationId)
                                    }
                                    "This Week" -> {
                                        val dayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK)
                                        val daysFromSunday = dayOfWeek - java.util.Calendar.SUNDAY
                                        val daysToSubtract = if (daysFromSunday < 0) daysFromSunday + 7 else daysFromSunday
                                        calendar.add(java.util.Calendar.DAY_OF_YEAR, -daysToSubtract)
                                        
                                        startDateMillis = calendar.timeInMillis
                                        endDateMillis = today
                                        onFilterChanged(null, Date(startDateMillis), null, selectedLocationId)
                                    }
                                    "This Month" -> {
                                        calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
                                        startDateMillis = calendar.timeInMillis
                                        endDateMillis = today
                                        onFilterChanged(null, Date(startDateMillis), null, selectedLocationId)
                                    }
                                    "Custom" -> {
                                        // No fetch
                                    }
                                }
                            }
                        )
                    }
                }
            }

            // Store Dropdown
            Box(modifier = Modifier.weight(1f)) {
                OutlinedButton(
                    onClick = { storeExpanded = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(selectedLocationName, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                        Icon(
                            painter = painterResource(android.R.drawable.arrow_down_float),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                DropdownMenu(
                    expanded = storeExpanded,
                    onDismissRequest = { storeExpanded = false }
                ) {
                    locations.forEach { (name, id) ->
                        DropdownMenuItem(
                            text = { Text(name) },
                            onClick = {
                                selectedLocationName = name
                                selectedLocationId = id
                                storeExpanded = false
                                // Trigger fetch with new location and current dates
                                if (selectedFilter == "This Week" || selectedFilter == "This Month") {
                                     onFilterChanged(null, Date(startDateMillis), null, selectedLocationId)
                                } else if (selectedFilter == "Custom") {
                                     onFilterChanged(null, Date(startDateMillis), Date(endDateMillis), selectedLocationId)
                                } else {
                                     onFilterChanged(null, Date(startDateMillis), Date(endDateMillis), selectedLocationId)
                                }
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { showStartDatePicker = true },
                enabled = selectedFilter == "Custom",
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (selectedFilter == "Custom") MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(dateFormatter.format(Date(startDateMillis)))
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }

            // End Date Button
            OutlinedButton(
                onClick = { showEndDatePicker = true },
                enabled = selectedFilter == "Custom",
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (selectedFilter == "Custom") MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(dateFormatter.format(Date(endDateMillis)))
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(painter = painterResource(android.R.drawable.arrow_down_float), contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }
        }
    }

    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = startDateMillis)
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { 
                        startDateMillis = it
                        onFilterChanged(null, Date(startDateMillis), Date(endDateMillis), selectedLocationId)
                    }
                    showStartDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = endDateMillis)
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { 
                        endDateMillis = it 
                        onFilterChanged(null, Date(startDateMillis), Date(endDateMillis), selectedLocationId)
                    }
                    showEndDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun SummarySection(totalAmount: Double, totalCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Card(
            modifier = Modifier
                .weight(1f)
                .border(1.dp, Color.Green, RoundedCornerShape(8.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = "Amount",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "₹ ${String.format("%.2f", totalAmount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Card(
            modifier = Modifier
                .weight(1f)
                .border(1.dp, Color.Blue, RoundedCornerShape(8.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Text("Count", style = MaterialTheme.typography.bodySmall)
                Text("$totalCount", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
        Box(
            modifier = Modifier
                .size(50.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                .clickable { /* Clear Filter */ },
            contentAlignment = Alignment.Center
        ) {
             Icon(painter = painterResource(android.R.drawable.ic_menu_close_clear_cancel), contentDescription = "Clear", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}



@Composable
fun SaleList(sales: List<SaleDto>) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(sales) { item ->
            SaleItemRow(item)
        }
    }
}

@Composable
fun SaleItemRow(item: SaleDto) {
    val displayDate = remember(item.createdAt) {
        try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            val date = item.createdAt?.let { parser.parse(it) }
            val formatter = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
            date?.let { formatter.format(it) } ?: item.createdAt ?: ""
        } catch (e: Exception) {
            item.createdAt ?: ""
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.billNumber ?: "Unknown",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "${item.paymentMode?.uppercase() ?: ""} | $displayDate",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "₹ ${item.amount ?: 0.0} | ${item.status?.uppercase() ?: ""}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (item.status == "paid") Color(0xFF4CAF50) else Color.Red,
                    fontWeight = FontWeight.Bold
                )
                 Icon(
                    painter = painterResource(android.R.drawable.checkbox_on_background),
                    contentDescription = null,
                    tint = if (item.status == "paid") Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = if (!item.editedBy.isNullOrEmpty()) "Edited by: ${item.editedBy}" else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
