package com.swadratna.swadratna_admin.ui.inventory

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageInventoryScreen(
    storeId: String,
    onNavigateBack: () -> Unit
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showAddIngredientDialog by remember { mutableStateOf(false) }
    var editingIngredient by remember { mutableStateOf<com.swadratna.swadratna_admin.data.model.Ingredient?>(null) }
    var deletingIngredient by remember { mutableStateOf<com.swadratna.swadratna_admin.data.model.Ingredient?>(null) }
    var stockInIngredient by remember { mutableStateOf<com.swadratna.swadratna_admin.data.model.Ingredient?>(null) }
    var stockOutIngredient by remember { mutableStateOf<com.swadratna.swadratna_admin.data.model.Ingredient?>(null) }
    var wastageIngredient by remember { mutableStateOf<com.swadratna.swadratna_admin.data.model.Ingredient?>(null) }
    var manualIngredient by remember { mutableStateOf<com.swadratna.swadratna_admin.data.model.Ingredient?>(null) }

    val viewModel: InventoryViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(storeId) {
        val sid = storeId.toIntOrNull() ?: 0
        viewModel.init(sid)
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
            FloatingActionButton(onClick = { showAddIngredientDialog = true }) {
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
                text = "Ingredients",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                if (uiState.isLoading) {
                    item {
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                } else if (uiState.error != null) {
                    item {
                        Text(
                            text = uiState.error ?: "",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else if (uiState.ingredients.isEmpty()) {
                    item {
                        Text(
                            text = "No ingredients found",
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    items(uiState.ingredients) { ing ->
                        IngredientCard(
                            ingredient = ing,
                            onEdit = { editingIngredient = ing },
                            onDelete = { deletingIngredient = ing },
                            onStockIn = { stockInIngredient = ing },
                            onStockOut = { stockOutIngredient = ing },
                            onWastage = { wastageIngredient = ing },
                            onManual = { manualIngredient = ing }
                        )
                    }
                }
            }
        }
    }

    if (showAddIngredientDialog) {
        AddIngredientDialog(
            onDismiss = { showAddIngredientDialog = false },
            onSave = { name, category, unit, reorderLevel, costPerUnit ->
                viewModel.createIngredient(
                    name = name,
                    category = category,
                    unit = unit,
                    reorderLevel = reorderLevel,
                    costPerUnit = costPerUnit
                ) { success, _ ->
                    if (success) {
                        showAddIngredientDialog = false
                    }
                }
            }
        )
    }

    if (editingIngredient != null) {
        EditIngredientDialog(
            ingredient = editingIngredient!!,
            onDismiss = { editingIngredient = null },
            onSave = { rl, cpu ->
                val id = editingIngredient?.id ?: return@EditIngredientDialog
                viewModel.updateIngredient(id, rl, cpu) { success, _ ->
                    if (success) editingIngredient = null
                }
            }
        )
    }

    if (deletingIngredient != null) {
        DeleteIngredientDialog(
            ingredient = deletingIngredient!!,
            onDismiss = { deletingIngredient = null },
            onConfirm = {
                val id = deletingIngredient?.id ?: return@DeleteIngredientDialog
                viewModel.deleteIngredient(id) { success, _ ->
                    if (success) deletingIngredient = null
                }
            }
        )
    }

    if (stockInIngredient != null) {
        StockInDialog(
            ingredient = stockInIngredient!!,
            onDismiss = { stockInIngredient = null },
            onSave = { qty, cpu, vendor, invoice, notes ->
                val id = stockInIngredient?.id ?: return@StockInDialog
                viewModel.stockIn(id, qty, cpu, vendor, invoice, notes) { success, _ ->
                    if (success) stockInIngredient = null
                }
            }
        )
    }

    if (stockOutIngredient != null) {
        StockOutDialog(
            ingredient = stockOutIngredient!!,
            onDismiss = { stockOutIngredient = null },
            onSave = { qty, reason ->
                val id = stockOutIngredient?.id ?: return@StockOutDialog
                viewModel.stockOut(id, qty, reason) { success, _ ->
                    if (success) stockOutIngredient = null
                }
            }
        )
    }

    if (wastageIngredient != null) {
        WastageDialog(
            ingredient = wastageIngredient!!,
            onDismiss = { wastageIngredient = null },
            onSave = { qty, reason ->
                val id = wastageIngredient?.id ?: return@WastageDialog
                viewModel.stockWastage(id, qty, reason) { success, _ ->
                    if (success) wastageIngredient = null
                }
            }
        )
    }

    if (manualIngredient != null) {
        ManualAdjustmentDialog(
            ingredient = manualIngredient!!,
            onDismiss = { manualIngredient = null },
            onSave = { newStock, reason ->
                val id = manualIngredient?.id ?: return@ManualAdjustmentDialog
                viewModel.stockAdjustment(id, newStock, reason) { success, _ ->
                    if (success) manualIngredient = null
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddIngredientDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String, Int, Double) -> Unit
) {
    val categories = listOf("grains", "vegetables", "dairy", "meat", "spices", "beverages", "other")
    val units = listOf("kg", "g", "litre", "ml", "piece")

    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var reorderLevel by remember { mutableStateOf("") }
    var costPerUnit by remember { mutableStateOf("") }

    var categoryExpanded by remember { mutableStateOf(false) }
    var unitExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Ingredient") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded }
                ) {
                    TextField(
                        readOnly = true,
                        value = category,
                        onValueChange = {},
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        categories.forEach { c ->
                            DropdownMenuItem(
                                text = { Text(c) },
                                onClick = {
                                    category = c
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = unitExpanded,
                    onExpandedChange = { unitExpanded = !unitExpanded }
                ) {
                    TextField(
                        readOnly = true,
                        value = unit,
                        onValueChange = {},
                        label = { Text("Unit") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = unitExpanded,
                        onDismissRequest = { unitExpanded = false }
                    ) {
                        units.forEach { u ->
                            DropdownMenuItem(
                                text = { Text(u) },
                                onClick = {
                                    unit = u
                                    unitExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = reorderLevel,
                    onValueChange = { reorderLevel = it },
                    label = { Text("Reorder Level") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = costPerUnit,
                    onValueChange = { costPerUnit = it },
                    label = { Text("Cost Per Unit") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val rl = reorderLevel.toIntOrNull() ?: 0
                val cpu = costPerUnit.toDoubleOrNull() ?: 0.0
                if (name.isNotBlank() && category.isNotBlank() && unit.isNotBlank()) {
                    onSave(name, category, unit, rl, cpu)
                }
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
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
fun IngredientCard(
    ingredient: com.swadratna.swadratna_admin.data.model.Ingredient,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onStockIn: () -> Unit,
    onStockOut: () -> Unit,
    onWastage: () -> Unit,
    onManual: () -> Unit
) {
    Card(elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = ingredient.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(text = "Category: ${ingredient.category}", style = MaterialTheme.typography.bodySmall)
                    Text(text = "Unit: ${ingredient.unit}", style = MaterialTheme.typography.bodySmall)
                }
                if (ingredient.id != null) {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete")
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Reorder: ${ingredient.reorderLevel}")
                Text(text = "Stock: ${ingredient.currentStock ?: 0}")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(text = "Cost: ₹${ingredient.costPerUnit}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(onClick = onStockIn) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Stock In")
                }
                FilledTonalButton(onClick = onStockOut) {
                    Icon(Icons.Filled.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Stock Out")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(onClick = onWastage) {
                    Icon(Icons.Filled.Warning, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Wastage")
                }
    FilledTonalButton(onClick = onManual) {
        Icon(Icons.Filled.Build, contentDescription = null)
        Spacer(modifier = Modifier.width(6.dp))
        Text("Manual")
    }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditIngredientDialog(
    ingredient: com.swadratna.swadratna_admin.data.model.Ingredient,
    onDismiss: () -> Unit,
    onSave: (Int, Double) -> Unit
) {
    var reorderLevel by remember { mutableStateOf(ingredient.reorderLevel.toString()) }
    var costPerUnit by remember { mutableStateOf(ingredient.costPerUnit.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Ingredient") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = reorderLevel,
                    onValueChange = { reorderLevel = it },
                    label = { Text("Reorder Level") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = costPerUnit,
                    onValueChange = { costPerUnit = it },
                    label = { Text("Cost Per Unit") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val rl = reorderLevel.toIntOrNull() ?: return@Button
                val cpu = costPerUnit.toDoubleOrNull() ?: return@Button
                onSave(rl, cpu)
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

 
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteIngredientDialog(
    ingredient: com.swadratna.swadratna_admin.data.model.Ingredient,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Ingredient") },
        text = { Text("Are you sure you want to delete ${ingredient.name}?") },
        confirmButton = {
            Button(onClick = onConfirm) { Text("Delete") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockInDialog(
    ingredient: com.swadratna.swadratna_admin.data.model.Ingredient,
    onDismiss: () -> Unit,
    onSave: (Int, Double, String?, String?, String?) -> Unit
) {
    var quantity by remember { mutableStateOf("") }
    var costPerUnit by remember { mutableStateOf(ingredient.costPerUnit.toString()) }
    var vendorName by remember { mutableStateOf("") }
    var invoiceNumber by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Stock In: ${ingredient.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = costPerUnit,
                    onValueChange = { costPerUnit = it },
                    label = { Text("Cost Per Unit") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = vendorName,
                    onValueChange = { vendorName = it },
                    label = { Text("Vendor Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = invoiceNumber,
                    onValueChange = { invoiceNumber = it },
                    label = { Text("Invoice Number") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val qty = quantity.toIntOrNull() ?: return@Button
                val cpu = costPerUnit.toDoubleOrNull() ?: return@Button
                onSave(qty, cpu, vendorName.ifBlank { null }, invoiceNumber.ifBlank { null }, notes.ifBlank { null })
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockOutDialog(
    ingredient: com.swadratna.swadratna_admin.data.model.Ingredient,
    onDismiss: () -> Unit,
    onSave: (Int, String?) -> Unit
) {
    var quantity by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Stock Out: ${ingredient.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val qty = quantity.toIntOrNull() ?: return@Button
                onSave(qty, reason.ifBlank { null })
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WastageDialog(
    ingredient: com.swadratna.swadratna_admin.data.model.Ingredient,
    onDismiss: () -> Unit,
    onSave: (Int, String?) -> Unit
) {
    var quantity by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Wastage: ${ingredient.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val qty = quantity.toIntOrNull() ?: return@Button
                onSave(qty, reason.ifBlank { null })
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualAdjustmentDialog(
    ingredient: com.swadratna.swadratna_admin.data.model.Ingredient,
    onDismiss: () -> Unit,
    onSave: (Int, String?) -> Unit
) {
    var newStock by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Manual Adjustment: ${ingredient.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = newStock,
                    onValueChange = { newStock = it },
                    label = { Text("New Stock") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val ns = newStock.toIntOrNull() ?: return@Button
                onSave(ns, reason.ifBlank { null })
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
