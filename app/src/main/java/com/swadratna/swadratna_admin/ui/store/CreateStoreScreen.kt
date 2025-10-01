package com.swadratna.swadratna_admin.ui.store

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.swadratna.swadratna_admin.data.model.StoreStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateStoreScreen(
    onNavigateBack: () -> Unit,
    storeId: String? = null,
    viewModel: StoreViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isEditMode = uiState.isEditMode
    val storeToEdit = uiState.storeToEdit
    
    // Reset edit mode when navigating away from the screen
    DisposableEffect(key1 = Unit) {
        onDispose {
            viewModel.onEvent(StoreEvent.ResetEditMode)
        }
    }
    
    // Trigger EditStore event when storeId is provided
    LaunchedEffect(key1 = storeId) {
        if (storeId != null) {
            viewModel.onEvent(StoreEvent.EditStore(storeId))
        }
    }
    
    var storeName by remember(storeToEdit) { mutableStateOf(storeToEdit?.name ?: "") }
    var storeLocation by remember(storeToEdit) { mutableStateOf(storeToEdit?.location ?: "") }
    var storeAddress by remember(storeToEdit) { mutableStateOf(storeToEdit?.address ?: "") }
    var storeStatus by remember(storeToEdit) { mutableStateOf(storeToEdit?.status ?: StoreStatus.ACTIVE) }
    var isProcessing by remember { mutableStateOf(false) }
    
    val scrollState = rememberScrollState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Store" else "Create New Store") },
                navigationIcon = {
                    IconButton(onClick = { 
                        viewModel.onEvent(StoreEvent.ResetEditMode)
                        onNavigateBack() 
                    }) {
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
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = storeName,
                onValueChange = { storeName = it },
                label = { Text("Store Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = storeLocation,
                onValueChange = { storeLocation = it },
                label = { Text("Store Location (City, State)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = storeAddress,
                onValueChange = { storeAddress = it },
                label = { Text("Store Address") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                minLines = 2
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Store Status",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Column {
                StatusRadioButton(
                    text = "Active",
                    selected = storeStatus == StoreStatus.ACTIVE,
                    onClick = { storeStatus = StoreStatus.ACTIVE }
                )
                
                StatusRadioButton(
                    text = "Inactive",
                    selected = storeStatus == StoreStatus.INACTIVE,
                    onClick = { storeStatus = StoreStatus.INACTIVE }
                )
                
                StatusRadioButton(
                    text = "Pending",
                    selected = storeStatus == StoreStatus.PENDING,
                    onClick = { storeStatus = StoreStatus.PENDING }
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = {
                    if (storeName.isNotBlank() && storeLocation.isNotBlank() && storeAddress.isNotBlank()) {
                        isProcessing = true
                        if (isEditMode && storeToEdit != null) {
                            viewModel.onEvent(StoreEvent.UpdateStore(
                                id = storeToEdit.id,
                                name = storeName,
                                location = storeLocation,
                                address = storeAddress,
                                status = storeStatus
                            ))
                        } else {
                            viewModel.onEvent(StoreEvent.CreateStore(storeName, storeLocation, storeAddress, storeStatus))
                        }
                        viewModel.onEvent(StoreEvent.ResetEditMode)
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = storeName.isNotBlank() && storeLocation.isNotBlank() && storeAddress.isNotBlank() && !isProcessing
            ) {
                Text(if (isEditMode) "Save Changes" else "Create Store")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun StatusRadioButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}