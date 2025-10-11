package com.swadratna.swadratna_admin.presentation.screens.menu

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.swadratna.swadratna_admin.data.model.MenuCategory
import com.swadratna.swadratna_admin.data.model.MenuItem
import com.swadratna.swadratna_admin.data.model.UpdateMenuItemRequest
import com.swadratna.swadratna_admin.presentation.viewmodels.MenuItemsViewModel
import com.swadratna.swadratna_admin.ui.menu.MenuCategoriesUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMenuItemScreen(
    menuItemId: Long,
    onNavigateBack: () -> Unit,
    viewModel: MenuItemsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val categoriesState by viewModel.categoriesState.collectAsState()
    val isUpdating by viewModel.isUpdatingMenuItem.collectAsState()
    
    var menuItem by remember { mutableStateOf<MenuItem?>(null) }
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var discountPercentage by remember { mutableStateOf("") }
    var displayOrder by remember { mutableStateOf("") }
    var allergenInfo by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<MenuCategory?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var updateInitiated by remember { mutableStateOf(false) }
    var isInitialized by remember { mutableStateOf(false) }
    
    // Navigation logic
    LaunchedEffect(isUpdating, updateInitiated) {
        if (updateInitiated && !isUpdating && uiState.error == null) {
            onNavigateBack()
        }
    }
    
    // Load categories and find the menu item
    LaunchedEffect(Unit) {
        viewModel.loadCategories()
        viewModel.loadMenuItems()
    }
    
    // Initialize form fields when menu item is found
    LaunchedEffect(uiState.menuItems, categoriesState) {
        if (!isInitialized && uiState.menuItems.isNotEmpty()) {
            val item = uiState.menuItems.find { it.id?.toLong() == menuItemId }
            if (item != null) {
                menuItem = item
                name = item.name
                description = item.description
                price = item.price.toString()
                discountPercentage = item.discountPercentage.toString()
                displayOrder = item.displayOrder.toString()
                allergenInfo = item.allergenInfo.joinToString(", ")
                selectedCategory = when (val catState = categoriesState) {
                    is MenuCategoriesUiState.Success -> catState.categories.find { it.id == item.categoryId }
                    else -> null
                }
                isInitialized = true
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Menu Item") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (!isInitialized) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Category Selection
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedCategory?.name ?: "",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Category *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        isError = selectedCategory == null && updateInitiated
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        when (val catState = categoriesState) {
                            is MenuCategoriesUiState.Success -> {
                                catState.categories.forEach { category ->
                                    DropdownMenuItem(
                                        text = { Text(category.name) },
                                        onClick = {
                                            selectedCategory = category
                                            expanded = false
                                        }
                                    )
                                }
                            }
                            else -> {
                                // Show loading or error state if needed
                            }
                        }
                    }
                }
                
                // Name Field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = name.isBlank() && updateInitiated
                )
                
                // Description Field
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description *") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    isError = description.isBlank() && updateInitiated
                )
                
                // Price Field
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price (INR) *") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = price.isBlank() && updateInitiated
                )
                
                // Discount Percentage Field
                OutlinedTextField(
                    value = discountPercentage,
                    onValueChange = { discountPercentage = it },
                    label = { Text("Discount Percentage") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    supportingText = { Text("Optional: 0-100") }
                )
                
                // Display Order Field
                OutlinedTextField(
                    value = displayOrder,
                    onValueChange = { displayOrder = it },
                    label = { Text("Display Order") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    supportingText = { Text("Optional: Order in menu") }
                )
                
                // Allergen Info Field
                OutlinedTextField(
                    value = allergenInfo,
                    onValueChange = { allergenInfo = it },
                    label = { Text("Allergen Information") },
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("Optional: Separate multiple allergens with commas") }
                )
                
                // Error Message
                uiState.error?.let { errorMessage ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                
                // Update Button
                Button(
                    onClick = {
                        updateInitiated = true
                        if (selectedCategory != null && name.isNotBlank() && description.isNotBlank() && price.isNotBlank() && menuItem != null) {
                            val priceValue = price.toDoubleOrNull()
                            val discountValue = discountPercentage.toIntOrNull() ?: 0
                            val orderValue = displayOrder.toIntOrNull() ?: 0
                            val allergens = if (allergenInfo.isNotBlank()) {
                                allergenInfo.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                            } else {
                                emptyList()
                            }
                            
                            if (priceValue != null && priceValue > 0) {
                                val currentMenuItem = menuItem
                                val currentCategory = selectedCategory
                                
                                if (currentMenuItem != null && currentCategory != null) {
                                    val itemId = currentMenuItem.id
                                    val categoryId = currentCategory.id
                                    
                                    if (itemId != null && categoryId != null) {
                                        val updateRequest = UpdateMenuItemRequest(
                                            id = itemId,
                                            tenantId = currentMenuItem.tenantId ?: 0,
                                            createdAt = currentMenuItem.createdAt ?: "",
                                            updatedAt = currentMenuItem.updatedAt ?: "",
                                            categoryId = categoryId,
                                            name = name,
                                            description = description,
                                            price = priceValue,
                                            currency = currentMenuItem.currency ?: "INR",
                                            discountPercentage = discountValue.toDouble(),
                                            discountedPrice = currentMenuItem.discountedPrice ?: 0.0,
                                            displayOrder = orderValue,
                                            image = currentMenuItem.image ?: "",
                                            isAvailable = currentMenuItem.isAvailable ?: true,
                                            allergenInfo = allergens,
                                            nutritionalInfo = currentMenuItem.nutritionalInfo,
                                            preparationTime = currentMenuItem.preparationTime ?: 0,
                                            spiceLevel = currentMenuItem.spiceLevel ?: "",
                                            tags = currentMenuItem.tags ?: emptyList()
                                        )
                                        viewModel.updateMenuItem(itemId, updateRequest)
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isUpdating
                ) {
                    if (isUpdating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("Update Menu Item")
                }
                
                // Required fields note
                Text(
                    text = "* Required fields",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}