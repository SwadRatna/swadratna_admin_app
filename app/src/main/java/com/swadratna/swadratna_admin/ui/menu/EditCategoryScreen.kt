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
fun EditCategoryScreen(
    categoryId: Long,
    onNavigateBack: () -> Unit,
    viewModel: MenuManagementViewModel = hiltViewModel()
) {
    val categoriesState by viewModel.categoriesState.collectAsState()

    // Find the category from the loaded list
    val existingCategory: MenuCategory? = remember(categoriesState, categoryId) {
        val success = categoriesState as? MenuCategoriesUiState.Success
        success?.categories?.firstOrNull { it.id?.toLong() == categoryId }
    }

    var name by remember(existingCategory) { mutableStateOf(existingCategory?.name ?: "") }
    var description by remember(existingCategory) { mutableStateOf(existingCategory?.description ?: "") }
    var displayOrder by remember(existingCategory) { mutableStateOf((existingCategory?.displayOrder ?: 1).toString()) }
    var isActive by remember(existingCategory) { mutableStateOf(existingCategory?.isActive ?: true) }

    var nameError by remember { mutableStateOf<String?>(null) }
    var descriptionError by remember { mutableStateOf<String?>(null) }
    var displayOrderError by remember { mutableStateOf<String?>(null) }

    fun validateForm(): Boolean {
        var isValid = true
        if (name.isBlank()) { nameError = "Category name is required"; isValid = false }
        if (description.isBlank()) { descriptionError = "Description is required"; isValid = false }
        if (displayOrder.isBlank()) {
            displayOrderError = "Display order is required"; isValid = false
        } else {
            try {
                val order = displayOrder.toInt()
                if (order < 1) {
                    displayOrderError = "Display order must be a positive number"; isValid = false
                }
            } catch (_: NumberFormatException) {
                displayOrderError = "Display order must be a valid number"; isValid = false
            }
        }
        return isValid
    }

    fun handleSubmit() {
        if (existingCategory?.id == null) {
            // If not found yet, avoid submitting
            return
        }
        if (validateForm()) {
            val updated = MenuCategory(
                id = existingCategory.id,
                name = name.trim(),
                description = description.trim(),
                displayOrder = displayOrder.toInt(),
                isActive = isActive
            )
            viewModel.updateCategory(updated)
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Edit Category") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
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
            if (existingCategory == null) {
                // Loading or not found message
                Text("Loading category details...")
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it; nameError = null },
                label = { Text("Category Name") },
                placeholder = { Text("e.g., Chinese, Italian, Desserts") },
                isError = nameError != null,
                supportingText = nameError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it; descriptionError = null },
                label = { Text("Description") },
                placeholder = { Text("Brief description of the category") },
                isError = descriptionError != null,
                supportingText = descriptionError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4
            )

            OutlinedTextField(
                value = displayOrder,
                onValueChange = { displayOrder = it; displayOrderError = null },
                label = { Text("Display Order") },
                placeholder = { Text("1, 2, 3...") },
                isError = displayOrderError != null,
                supportingText = displayOrderError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

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

            Button(
                onClick = { handleSubmit() },
                modifier = Modifier.fillMaxWidth(),
                enabled = existingCategory != null
            ) {
                Text("Save Changes")
            }
        }
    }
}