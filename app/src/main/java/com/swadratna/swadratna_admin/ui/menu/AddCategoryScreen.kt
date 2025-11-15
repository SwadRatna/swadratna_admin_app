package com.swadratna.swadratna_admin.ui.menu

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.swadratna.swadratna_admin.data.model.MenuCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCategoryScreen(
    viewModel: MenuManagementViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onCategoryAdded: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var displayOrder by remember { mutableStateOf("1") }
    var isActive by remember { mutableStateOf(true) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var descriptionError by remember { mutableStateOf<String?>(null) }
    var displayOrderError by remember { mutableStateOf<String?>(null) }
    var creationInitiated by remember { mutableStateOf(false) }

    val isCreating by viewModel.isCreatingCategory.collectAsState()
    val categoriesState by viewModel.categoriesState.collectAsState()
    val categorySuccessMessage = when (categoriesState) {
        is MenuCategoriesUiState.Success -> (categoriesState as MenuCategoriesUiState.Success).successMessage
        else -> null
    }
    
    LaunchedEffect(categorySuccessMessage, creationInitiated, isCreating) {
        if (creationInitiated && !isCreating && categorySuccessMessage != null) {
            onCategoryAdded()
        }
    }

    fun validateForm(): Boolean {
        var isValid = true
        
        if (name.isBlank()) {
            nameError = "Category name is required"
            isValid = false
        }
        
        if (description.isBlank()) {
            descriptionError = "Description is required"
            isValid = false
        }
        
        if (displayOrder.isBlank()) {
            displayOrderError = "Display order is required"
            isValid = false
        } else {
            try {
                val order = displayOrder.toInt()
                if (order < 1) {
                    displayOrderError = "Display order must be a positive number"
                    isValid = false
                }
            } catch (e: NumberFormatException) {
                displayOrderError = "Display order must be a valid number"
                isValid = false
            }
        }
        
        return isValid
    }

    fun handleSubmit() {
        if (validateForm()) {
            val category = MenuCategory(
                name = name.trim(),
                description = description.trim(),
                displayOrder = displayOrder.toInt(),
                isActive = isActive
            )
            creationInitiated = true
            viewModel.createCategory(category)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Add Category") },
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
            // Category Name Field
            OutlinedTextField(
                value = name,
                onValueChange = { 
                    name = it
                    nameError = null
                },
                label = { Text("Category Name") },
                placeholder = { Text("e.g., Chinese, Italian, Desserts") },
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
                placeholder = { Text("Brief description of the category") },
                isError = descriptionError != null,
                supportingText = descriptionError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4
            )

            // Display Order Field
            OutlinedTextField(
                value = displayOrder,
                onValueChange = { 
                    displayOrder = it
                    displayOrderError = null
                },
                label = { Text("Display Order") },
                placeholder = { Text("1, 2, 3...") },
                isError = displayOrderError != null,
                supportingText = displayOrderError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            // Active Status Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Active Status",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Whether this category should be visible to customers",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isActive,
                    onCheckedChange = { isActive = it }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Submit Button
            Button(
                onClick = { handleSubmit() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isCreating
            ) {
                if (isCreating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (isCreating) "Creating..." else "Create Category")
            }
        }
    }
}