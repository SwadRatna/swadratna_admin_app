package com.swadratna.swadratna_admin.ui.sales

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.swadratna.swadratna_admin.R
import com.swadratna.swadratna_admin.data.model.SaleDto
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaleListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToNewSale: () -> Unit = {},
    onNavigateToVisualize: () -> Unit = {},
    viewModel: SalesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Helper to format date for API
    val apiDateFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }

    // Initial fetch
    LaunchedEffect(Unit) {
        viewModel.fetchSales()
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
                        Text(
                            text = "FAST v39.0 | 7906897228 | 1913",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
                    }
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = { }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
        ) {
            // Filter Section
            FilterSection(
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
                // Summary Section
                SummarySection(
                    totalAmount = uiState.salesResponse?.summary?.totalAmount ?: 0.0,
                    totalCount = uiState.salesResponse?.summary?.count ?: 0
                )

                // Sales List
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
    onFilterChanged: (Date?, Date?, Date?, String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("Today") }
    val filters = listOf("Today", "Yesterday", "This Week", "This Month", "Custom")

    // Store/Location State
    var storeExpanded by remember { mutableStateOf(false) }
    val locations = listOf("Swad Ratna" to "1000003", "All Locations" to null)
    var selectedLocationName by remember { mutableStateOf(locations[0].first) }
    var selectedLocationId by remember { mutableStateOf(locations[0].second) }

    // Date pickers state
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    
    // Initial dates (mocked as today for now)
    var startDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var endDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }

    val dateFormatter = remember { SimpleDateFormat("dd/MM/yy", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // Top Row: Date Filter Dropdown | Store Dropdown
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Date Filter Dropdown
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
                        Text(selectedFilter, color = Color.Black, maxLines = 1)
                        Icon(
                            painter = painterResource(android.R.drawable.arrow_down_float),
                            contentDescription = null,
                            tint = Color.Black
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
                        Text(selectedLocationName, color = Color.Black, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                        Icon(
                            painter = painterResource(android.R.drawable.arrow_down_float),
                            contentDescription = null,
                            tint = Color.Black
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
            // Start Date Button
            OutlinedButton(
                onClick = { showStartDatePicker = true },
                enabled = selectedFilter == "Custom",
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (selectedFilter == "Custom") Color.Black else Color.Gray,
                    disabledContentColor = Color.Gray
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
                    contentColor = if (selectedFilter == "Custom") Color.Black else Color.Gray,
                    disabledContentColor = Color.Gray
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
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Text("Amount", style = MaterialTheme.typography.bodySmall)
                Text("₹ $totalAmount", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
        Card(
            modifier = Modifier
                .weight(1f)
                .border(1.dp, Color.Blue, RoundedCornerShape(8.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White),
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
                .background(Color.LightGray, RoundedCornerShape(8.dp))
                .clickable { /* Clear Filter */ },
            contentAlignment = Alignment.Center
        ) {
             Icon(painter = painterResource(android.R.drawable.ic_menu_close_clear_cancel), contentDescription = "Clear", tint = Color.Gray)
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
    // Date parsing
    val displayDate = remember(item.createdAt) {
        try {
            // Adjust format to handle the Z literal or timezone if needed. 
            // "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'" matches the input literal 'Z'
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color.LightGray)
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
                Text(
                    text = "${item.paymentMode?.uppercase() ?: ""} | $displayDate",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
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
                    tint = if (item.status == "paid") Color(0xFF4CAF50) else Color.Gray,
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
                    color = Color.Gray,
                    fontSize = 10.sp,
                    modifier = Modifier.weight(1f)
                )
                OutlinedButton(
                    onClick = { /* TODO */ },
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Sale Return", fontSize = 10.sp)
                }
            }
        }
    }
}
