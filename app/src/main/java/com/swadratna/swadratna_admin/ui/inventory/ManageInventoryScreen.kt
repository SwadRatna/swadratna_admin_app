package com.swadratna.swadratna_admin.ui.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageInventoryScreen(
    storeId: String,
    onNavigateBack: () -> Unit
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<InventoryItem?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    // Mock Data
    val inventoryItems = remember {
        mutableStateListOf(
            InventoryItem("1", "Rice", 50.0, "kg", 2500.0),
            InventoryItem("2", "Oil", 20.0, "L", 3000.0),
            InventoryItem("3", "Chicken", 10.0, "kg", 2000.0),
            InventoryItem("4", "Spices", 2.0, "kg", 1500.0)
        )
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.toEpochDay() * 24 * 60 * 60 * 1000
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        selectedDate = LocalDate.ofEpochDay(it / (24 * 60 * 60 * 1000))
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Inventory") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Inventory")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Date Scroller
            DateScroller(
                selectedDate = selectedDate,
                onDateSelected = { selectedDate = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Summary Section (Optional but helpful based on description)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Summary for ${selectedDate.format(DateTimeFormatter.ofPattern("MMM dd"))}", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Added Value:")
                        Text("₹9,000", fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Spent Value:") // Assuming spent means used
                        Text("₹2,000", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Inventory Items",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                items(inventoryItems) { item ->
                    InventoryItemCard(
                        item = item,
                        onEdit = { editingItem = item }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddInventoryDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, quantity, unit, value ->
                inventoryItems.add(
                    InventoryItem(
                        id = System.currentTimeMillis().toString(),
                        name = name,
                        quantity = quantity,
                        unit = unit,
                        totalValue = value
                    )
                )
                showAddDialog = false
            }
        )
    }

    if (editingItem != null) {
        AddInventoryDialog(
            item = editingItem,
            onDismiss = { editingItem = null },
            onSave = { name, quantity, unit, value ->
                val index = inventoryItems.indexOfFirst { it.id == editingItem?.id }
                if (index != -1) {
                    inventoryItems[index] = inventoryItems[index].copy(
                        name = name,
                        quantity = quantity,
                        unit = unit,
                        totalValue = value
                    )
                }
                editingItem = null
            }
        )
    }
}

@Composable
fun DateScroller(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    // Generate 2 weeks range centered on selectedDate
    val dates = remember(selectedDate) {
        (-7..7).map { selectedDate.plusDays(it.toLong()) }
    }
    
    val listState = rememberLazyListState()
    
    // Scroll to center initially
    LaunchedEffect(selectedDate) {
        listState.scrollToItem(7) // Center is at index 7 (0 to 14 items, 7 is middle)
    }
    
    LazyRow(
        state = listState,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(dates) { date ->
            val isSelected = date == selectedDate
            Card(
                modifier = Modifier
                    .clickable { onDateSelected(date) },
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = date.format(DateTimeFormatter.ofPattern("EEE")),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = date.dayOfMonth.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun InventoryItemCard(
    item: InventoryItem,
    onEdit: () -> Unit
) {
    var quantity by remember(item.quantity) { mutableDoubleStateOf(item.quantity) }

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = item.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(text = "Unit: ${item.unit}", style = MaterialTheme.typography.bodySmall)
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "₹${item.totalValue}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { if (quantity > 0) quantity-- },
                        modifier = Modifier.size(32.dp).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                    ) {
                        Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Decrease", modifier = Modifier.size(16.dp)) 
                    }
                    
                    Text(
                        text = "$quantity ${item.unit}",
                        modifier = Modifier.padding(horizontal = 12.dp),
                        fontWeight = FontWeight.Bold
                    )
                    
                    IconButton(
                        onClick = { quantity++ },
                        modifier = Modifier.size(32.dp).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                    ) {
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Increase", modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddInventoryDialog(
    item: InventoryItem? = null,
    onDismiss: () -> Unit,
    onSave: (String, Double, String, Double) -> Unit
) {
    var name by remember { mutableStateOf(item?.name ?: "") }
    var quantity by remember { mutableStateOf(item?.quantity?.toString() ?: "") }
    var unit by remember { mutableStateOf(item?.unit ?: "") }
    var totalValue by remember { mutableStateOf(item?.totalValue?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (item == null) "Add Inventory" else "Edit Inventory") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("Quantity") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unit") },
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = totalValue,
                    onValueChange = { totalValue = it },
                    label = { Text("Total Value (₹)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val qty = quantity.toDoubleOrNull() ?: 0.0
                val value = totalValue.toDoubleOrNull() ?: 0.0
                if (name.isNotBlank() && qty > 0 && unit.isNotBlank()) {
                    onSave(name, qty, unit, value)
                }
            }) {
                Text(if (item == null) "Add" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

data class InventoryItem(
    val id: String,
    val name: String,
    val quantity: Double,
    val unit: String,
    val totalValue: Double
)
