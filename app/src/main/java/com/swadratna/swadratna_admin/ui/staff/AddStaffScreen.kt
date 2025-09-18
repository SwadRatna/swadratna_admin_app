package com.swadratna.swadratna_admin.ui.staff

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
import com.swadratna.swadratna_admin.model.Staff
import com.swadratna.swadratna_admin.model.StaffStatus
import com.swadratna.swadratna_admin.model.WorkingHours
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStaffScreen(
    onNavigateBack: () -> Unit,
    storeId: String,
    viewModel: StaffManagementViewModel = hiltViewModel()
) {
    var staffName by remember { mutableStateOf("") }
    var staffPosition by remember { mutableStateOf("") }
    var staffStatus by remember { mutableStateOf(StaffStatus.ACTIVE) }
    var startTimeHour by remember { mutableStateOf("09") }
    var startTimeMinute by remember { mutableStateOf("00") }
    var endTimeHour by remember { mutableStateOf("17") }
    var endTimeMinute by remember { mutableStateOf("00") }
    var isCreating by remember { mutableStateOf(false) }
    
    val scrollState = rememberScrollState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Staff") },
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
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = staffName,
                onValueChange = { staffName = it },
                label = { Text("Staff Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = staffPosition,
                onValueChange = { staffPosition = it },
                label = { Text("Position") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Working Hours",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Start Time",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row {
                        OutlinedTextField(
                            value = startTimeHour,
                            onValueChange = { 
                                if (it.length <= 2 && it.all { char -> char.isDigit() }) {
                                    val hour = it.toIntOrNull() ?: 0
                                    if (hour in 0..23) {
                                        startTimeHour = it.padStart(2, '0')
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            label = { Text("HH") }
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = ":",
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        OutlinedTextField(
                            value = startTimeMinute,
                            onValueChange = { 
                                if (it.length <= 2 && it.all { char -> char.isDigit() }) {
                                    val minute = it.toIntOrNull() ?: 0
                                    if (minute in 0..59) {
                                        startTimeMinute = it.padStart(2, '0')
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            label = { Text("MM") }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "End Time",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row {
                        OutlinedTextField(
                            value = endTimeHour,
                            onValueChange = { 
                                if (it.length <= 2 && it.all { char -> char.isDigit() }) {
                                    val hour = it.toIntOrNull() ?: 0
                                    if (hour in 0..23) {
                                        endTimeHour = it.padStart(2, '0')
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            label = { Text("HH") }
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = ":",
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        OutlinedTextField(
                            value = endTimeMinute,
                            onValueChange = { 
                                if (it.length <= 2 && it.all { char -> char.isDigit() }) {
                                    val minute = it.toIntOrNull() ?: 0
                                    if (minute in 0..59) {
                                        endTimeMinute = it.padStart(2, '0')
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            label = { Text("MM") }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Staff Status",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Column {
                StatusRadioButton(
                    text = "Active",
                    selected = staffStatus == StaffStatus.ACTIVE,
                    onClick = { staffStatus = StaffStatus.ACTIVE }
                )
                
                StatusRadioButton(
                    text = "Inactive",
                    selected = staffStatus == StaffStatus.INACTIVE,
                    onClick = { staffStatus = StaffStatus.INACTIVE }
                )
                
                StatusRadioButton(
                    text = "On Break",
                    selected = staffStatus == StaffStatus.ON_BREAK,
                    onClick = { staffStatus = StaffStatus.ON_BREAK }
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = {
                    if (staffName.isNotBlank() && staffPosition.isNotBlank()) {
                        isCreating = true
                        val startTime = LocalTime.of(
                            startTimeHour.toInt(),
                            startTimeMinute.toInt()
                        )
                        val endTime = LocalTime.of(
                            endTimeHour.toInt(),
                            endTimeMinute.toInt()
                        )
                        val workingHours = WorkingHours(startTime, endTime)
                        
                        // TODO: Implement adding staff functionality in ViewModel
                        // viewModel.addStaff(staffName, staffPosition, staffStatus, workingHours, storeId)
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = staffName.isNotBlank() && staffPosition.isNotBlank() && !isCreating
            ) {
                Text("Add Staff")
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