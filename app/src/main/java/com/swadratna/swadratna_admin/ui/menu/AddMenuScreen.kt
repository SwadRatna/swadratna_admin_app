package com.swadratna.swadratna_admin.ui.menu

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.swadratna.swadratna_admin.data.model.MenuCategory
import com.swadratna.swadratna_admin.data.model.MenuItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMenuScreen(
    viewModel: MenuManagementViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onMenuAdded: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var availability by remember { mutableStateOf("All Day") }
    var isAvailable by remember { mutableStateOf(true) }
    var selectedCategory by remember { mutableStateOf<MenuCategory?>(null) }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    
    // Error states
    var nameError by remember { mutableStateOf<String?>(null) }
    var descriptionError by remember { mutableStateOf<String?>(null) }
    var priceError by remember { mutableStateOf<String?>(null) }
    var categoryError by remember { mutableStateOf<String?>(null) }

    val categoriesState by viewModel.categoriesState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadCategories()
    }

    fun validateForm(): Boolean {
        var isValid = true
        
        nameError = when {
            name.isBlank() -> {
                isValid = false
                "Menu item name is required"
            }
            name.length < 2 -> {
                isValid = false
                "Name must be at least 2 characters"
            }
            else -> null
        }
        
        descriptionError = when {
            description.isBlank() -> {
                isValid = false
                "Description is required"
            }
            description.length < 10 -> {
                isValid = false
                "Description must be at least 10 characters"
            }
            else -> null
        }
        
        priceError = when {
            price.isBlank() -> {
                isValid = false
                "Price is required"
            }
            price.toDoubleOrNull() == null -> {
                isValid = false
                "Price must be a valid number"
            }
            price.toDoubleOrNull()!! <= 0 -> {
                isValid = false
                "Price must be greater than 0"
            }
            else -> null
        }
        
        categoryError = if (selectedCategory == null) {
            isValid = false
            "Please select a category"
        } else null
        
        return isValid
    }

    fun handleSubmit() {
        if (validateForm()) {
            // Here you would typically call a repository method to create the menu item
            // For now, we'll just navigate back
            onMenuAdded()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Add Menu Item") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Menu Item Name Field
            OutlinedTextField(
                value = name,
                onValueChange = { 
                    name = it
                    nameError = null
                },
                label = { Text("Menu Item Name") },
                placeholder = { Text("e.g., Margherita Pizza, Caesar Salad") },
                isError = nameError != null,
                supportingText = nameError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Description Field
            OutlinedTextField(
                value = description,
                onValueChange = { 
                    description = it
                    descriptionError = null
                },
                label = { Text("Description") },
                placeholder = { Text("Detailed description of the menu item") },
                isError = descriptionError != null,
                supportingText = descriptionError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )

            // Price Field
            OutlinedTextField(
                value = price,
                onValueChange = { 
                    price = it
                    priceError = null
                },
                label = { Text("Price") },
                placeholder = { Text("0.00") },
                isError = priceError != null,
                supportingText = priceError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                leadingIcon = { Text("$") }
            )

            // Category Selection
            ExposedDropdownMenuBox(
                expanded = showCategoryDropdown,
                onExpandedChange = { showCategoryDropdown = !showCategoryDropdown }
            ) {
                OutlinedTextField(
                    value = selectedCategory?.name ?: "",
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Category") },
                    placeholder = { Text("Select a category") },
                    isError = categoryError != null,
                    supportingText = categoryError?.let { { Text(it) } },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Dropdown"
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                when (categoriesState) {
                    is MenuCategoriesUiState.Success -> {
                        ExposedDropdownMenu(
                            expanded = showCategoryDropdown,
                            onDismissRequest = { showCategoryDropdown = false }
                        ) {
                            (categoriesState as MenuCategoriesUiState.Success).categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { 
                                        Column {
                                            Text(category.name)
                                            Text(
                                                text = category.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedCategory = category
                                        categoryError = null
                                        showCategoryDropdown = false
                                    }
                                )
                            }
                        }
                    }
                    is MenuCategoriesUiState.Loading -> {
                        ExposedDropdownMenu(
                            expanded = showCategoryDropdown,
                            onDismissRequest = { showCategoryDropdown = false }
                        ) {
                            DropdownMenuItem(
                                text = { 
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Loading categories...")
                                    }
                                },
                                onClick = { }
                            )
                        }
                    }
                    is MenuCategoriesUiState.Error -> {
                        ExposedDropdownMenu(
                            expanded = showCategoryDropdown,
                            onDismissRequest = { showCategoryDropdown = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Error loading categories") },
                                onClick = { showCategoryDropdown = false }
                            )
                        }
                    }
                }
            }

            // Availability Field
            OutlinedTextField(
                value = availability,
                onValueChange = { availability = it },
                label = { Text("Availability") },
                placeholder = { Text("e.g., All Day, Lunch Only, Dinner Only") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Available Status Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Available Now",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Whether this item is currently available for ordering",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isAvailable,
                    onCheckedChange = { isAvailable = it }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Submit Button
            Button(
                onClick = { handleSubmit() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Menu Item")
            }
        }
    }
}