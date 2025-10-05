package com.swadratna.swadratna_admin.presentation.screens.menu

import android.util.Log
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
import com.swadratna.swadratna_admin.data.model.CreateMenuItemRequest
import com.swadratna.swadratna_admin.presentation.viewmodels.MenuItemsViewModel
import com.swadratna.swadratna_admin.ui.menu.MenuCategoriesUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMenuItemScreen(
    onNavigateBack: () -> Unit,
    viewModel: MenuItemsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val categoriesState by viewModel.categoriesState.collectAsState()
    val isCreating by viewModel.isCreatingMenuItem.collectAsState()

    
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var discountPercentage by remember { mutableStateOf("") }
    var displayOrder by remember { mutableStateOf("") }
    var allergenInfo by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<MenuCategory?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var creationInitiated by remember { mutableStateOf(false) }
    
    LaunchedEffect(isCreating, creationInitiated, uiState.error) {
        if (creationInitiated && !isCreating && uiState.error == null) {
            kotlinx.coroutines.delay(500)
            onNavigateBack()
        }
    }
    
    LaunchedEffect(Unit) {
        viewModel.loadCategories()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Menu Item") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
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
                    isError = selectedCategory == null && creationInitiated
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    when (val catState = categoriesState) {
                        is MenuCategoriesUiState.Success -> {
                            for (category in catState.categories) {
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
                            DropdownMenuItem(
                                text = { Text("Loading categories...") },
                                onClick = { }
                            )
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
                isError = name.isBlank() && creationInitiated
            )
            
            // Description Field
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description *") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                isError = description.isBlank() && creationInitiated
            )
            
            // Price Field
            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Price (INR) *") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = price.isBlank() && creationInitiated
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

            Button(
                onClick = {
                    creationInitiated = true
                    if (name.isNotBlank() && description.isNotBlank() && price.isNotBlank()) {
                        val priceValue = price.toDoubleOrNull()
                        val discountValue = discountPercentage.toDoubleOrNull() ?: 0.0
                        val orderValue = displayOrder.toIntOrNull() ?: 0
                        val allergens = if (allergenInfo.isNotBlank()) {
                            allergenInfo.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        } else {
                            emptyList()
                        }
                        
                        val selectedCat = selectedCategory
                        val categoryId = selectedCat?.id
                        if (priceValue != null && priceValue > 0 && categoryId != null) {
                            val createRequest = CreateMenuItemRequest(
                                categoryId = categoryId,
                                name = name,
                                description = description,
                                price = priceValue,
                                discountPercentage = discountValue.toDouble(),
                                displayOrder = orderValue,
                                allergenInfo = allergens
                            )
                            viewModel.createMenuItem(createRequest)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isCreating
            ) {
                if (isCreating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Create Menu Item")
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