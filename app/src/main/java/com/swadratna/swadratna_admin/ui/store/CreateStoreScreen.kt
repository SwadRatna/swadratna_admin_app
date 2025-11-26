package com.swadratna.swadratna_admin.ui.store

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.swadratna.swadratna_admin.data.model.Store

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateStoreScreen(
    onNavigateBack: () -> Unit,
    storeId: String? = null,
    viewModel: StoreViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val storeToEdit = storeId?.let { id ->
        uiState.stores.find { it.id.toString() == id }
    }
    // State variables for form fields based on API structure
    var branchName by remember { mutableStateOf("") }
    var plotNo by remember { mutableStateOf("") }
    var poBoxNo by remember { mutableStateOf("") }
    var street1 by remember { mutableStateOf("") }
    var street2 by remember { mutableStateOf("") }
    var locality by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var pincode by remember { mutableStateOf("") }
    var landmark by remember { mutableStateOf("") }
    var locationMobileNumber by remember { mutableStateOf("") }
    var numberOfTables by remember { mutableStateOf("") }
    
    // Update form fields when storeToEdit changes
    LaunchedEffect(storeToEdit) {
        if (storeToEdit != null) {
            branchName = storeToEdit.name
            plotNo = storeToEdit.address?.plotNo ?: ""
            poBoxNo = storeToEdit.address?.poBoxNo ?: ""
            street1 = storeToEdit.address?.street1 ?: ""
            street2 = storeToEdit.address?.street2 ?: ""
            locality = storeToEdit.address?.locality ?: ""
            city = storeToEdit.address?.city ?: ""
            pincode = storeToEdit.address?.pincode ?: ""
            landmark = storeToEdit.address?.landmark ?: ""
            locationMobileNumber = storeToEdit.locationMobileNumber ?: ""
            numberOfTables = storeToEdit.numberOfTables?.toString() ?: ""

        } else {
            // Reset form for new store creation
            branchName = ""
            plotNo = ""
            poBoxNo = ""
            street1 = ""
            street2 = ""
            locality = ""
            city = ""
            pincode = ""
            landmark = ""
            locationMobileNumber = ""
            numberOfTables = ""
            viewModel.onEvent(StoreEvent.ResetEditMode)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(if (storeToEdit != null) "Edit Store" else "Create Store") 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
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
            Text(
                text = "Address Information",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // Plot Number Field
            OutlinedTextField(
                value = plotNo,
                onValueChange = { plotNo = it },
                label = { Text("Plot Number") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // PO Box Number Field
            OutlinedTextField(
                value = poBoxNo,
                onValueChange = { poBoxNo = it },
                label = { Text("PO Box Number") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // Street 1 Field
            OutlinedTextField(
                value = street1,
                onValueChange = { street1 = it },
                label = { Text("Street 1 *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // Street 2 Field
            OutlinedTextField(
                value = street2,
                onValueChange = { street2 = it },
                label = { Text("Street 2") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // Locality Field
            OutlinedTextField(
                value = locality,
                onValueChange = { locality = it },
                label = { Text("Locality *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // City Field
            OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                label = { Text("City *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // Pincode Field
            OutlinedTextField(
                value = pincode,
                onValueChange = { pincode = it },
                label = { Text("Pincode *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            
            // Landmark Field
            OutlinedTextField(
                value = landmark,
                onValueChange = { landmark = it },
                label = { Text("Landmark") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Text(
                text = "Store Information",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
            
            OutlinedTextField(
                value = branchName,
                onValueChange = { branchName = it },
                label = { Text("Branch Name *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Location Mobile Number Field
            OutlinedTextField(
                value = locationMobileNumber,
                onValueChange = { locationMobileNumber = it },
                label = { Text("Mobile Number *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )
            
            // Number of Tables Field
            OutlinedTextField(
                value = numberOfTables,
                onValueChange = { numberOfTables = it },
                label = { Text("Number of Tables *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            
            // Error message
            if (uiState.error != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = uiState.error ?: "",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Create/Update Button
            Button(
                onClick = {
                    val tablesCount = numberOfTables.toIntOrNull()
                    
                    if (branchName.isBlank() || street1.isBlank() || locality.isBlank() || city.isBlank() || 
                        pincode.isBlank() || locationMobileNumber.isBlank() || tablesCount == null) {
                        // Show validation error
                        return@Button
                    }
                    
                    if (storeToEdit != null) {
                        // Update existing store
                        viewModel.onEvent(
                            StoreEvent.UpdateStore(
                                storeId = storeToEdit.id,
                                name = branchName,
                                plotNo = plotNo,
                                poBoxNo = poBoxNo,
                                street1 = street1,
                                street2 = street2,
                                locality = locality,
                                city = city,
                                pincode = pincode,
                                landmark = landmark,
                                locationMobileNumber = locationMobileNumber,
                                numberOfTables = tablesCount
                            )
                        )
                    } else {
                        // Create new store
                        viewModel.onEvent(
                            StoreEvent.CreateStore(
                                name = branchName,
                                plotNo = plotNo,
                                poBoxNo = poBoxNo,
                                street1 = street1,
                                street2 = street2,
                                locality = locality,
                                city = city,
                                pincode = pincode,
                                landmark = landmark,
                                locationMobileNumber = locationMobileNumber,
                                numberOfTables = tablesCount
                            )
                        )
                    }
                    
                    // Navigate back after successful operation
                    if (!uiState.isLoading) {
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading && branchName.isNotBlank() && street1.isNotBlank() && locality.isNotBlank() && 
                         city.isNotBlank() && pincode.isNotBlank() && locationMobileNumber.isNotBlank() && 
                         numberOfTables.toIntOrNull() != null
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (storeToEdit != null) "Update Store" else "Create Store")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
