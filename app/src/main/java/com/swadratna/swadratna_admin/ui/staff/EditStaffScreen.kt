package com.swadratna.swadratna_admin.ui.staff

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.swadratna.swadratna_admin.data.model.Staff

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditStaffScreen(
    staffId: String,
    storeId: String,
    modifier: Modifier = Modifier,
    viewModel: StaffManagementViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Load staff data when screen initializes
    LaunchedEffect(storeId) {
        viewModel.loadStaff(storeId.toIntOrNull() ?: 0)
    }
    
    // Find the staff member to edit
    val staffToEdit = uiState.staffList.find { it.id == staffId.toIntOrNull() }
    
    // Form state variables
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var salary by remember { mutableStateOf("") }
    var joinDate by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("active") }
    
    // Dropdown state for role selection
    var roleDropdownExpanded by remember { mutableStateOf(false) }
    val roleOptions = listOf("manager", "waiter", "chef", "cashier")
    
    // Error states
    var nameError by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    var phoneError by remember { mutableStateOf("") }
    var addressError by remember { mutableStateOf("") }
    var roleError by remember { mutableStateOf("") }
    var salaryError by remember { mutableStateOf("") }
    var joinDateError by remember { mutableStateOf("") }
    var startTimeError by remember { mutableStateOf("") }
    var endTimeError by remember { mutableStateOf("") }
    
    // Track if update was initiated to handle navigation
    var updateInitiated by remember { mutableStateOf(false) }
    
    // Initialize form with existing staff data
    LaunchedEffect(staffToEdit) {
        staffToEdit?.let { staff ->
            name = staff.name ?: ""
            email = staff.email ?: ""
            phone = staff.phone ?: staff.mobileNumber ?: ""
            address = staff.address ?: ""
            role = staff.position ?: ""
            salary = staff.salary?.toString() ?: ""
            joinDate = staff.joinDate ?: ""
            
            // Handle working hours from either workingHours or shiftTiming
            startTime = (staff.workingHours?.startTime ?: staff.shiftTiming?.startTime) ?: ""
            endTime = (staff.workingHours?.endTime ?: staff.shiftTiming?.endTime) ?: ""
            
            status = when (staff.status.name.lowercase()) {
                "active" -> "active"
                "inactive" -> "inactive"
                else -> "active"
            }
        }
    }
    
    // Navigate back after successful update
    LaunchedEffect(uiState.isLoading, uiState.error, updateInitiated) {
        if (updateInitiated && !uiState.isLoading && uiState.error == null) {
            onNavigateBack()
        }
    }
    
    // Validation functions
    fun validateEmail(email: String): Boolean {
        return email.contains("@") && email.contains(".")
    }
    
    fun validatePhone(phone: String): Boolean {
        return phone.length >= 10 && phone.all { it.isDigit() }
    }
    
    fun validateSalary(salary: String): Boolean {
        return salary.toDoubleOrNull() != null && salary.toDouble() > 0
    }
    
    fun validateTime(time: String): Boolean {
        val hour = time.toIntOrNull()
        return hour != null && hour in 0..23
    }
    
    fun validateJoinDate(date: String): Boolean {
        val regex = Regex("^\\d{2}/\\d{2}/\\d{4}$")
        return regex.matches(date)
    }
    
    fun validateForm(): Boolean {
        var isValid = true
        
        if (name.isBlank()) {
            nameError = "Name is required"
            isValid = false
        }
        
        if (email.isBlank()) {
            emailError = "Email is required"
            isValid = false
        } else if (!validateEmail(email)) {
            emailError = "Please enter a valid email"
            isValid = false
        }
        
        if (phone.isBlank()) {
            phoneError = "Phone is required"
            isValid = false
        } else if (!validatePhone(phone)) {
            phoneError = "Please enter a valid 10-digit phone number"
            isValid = false
        }
        
        if (address.isBlank()) {
            addressError = "Address is required"
            isValid = false
        }
        
        if (role.isBlank()) {
            roleError = "Role is required"
            isValid = false
        }
        
        if (salary.isBlank()) {
            salaryError = "Salary is required"
            isValid = false
        } else if (!validateSalary(salary)) {
            salaryError = "Please enter a valid salary amount"
            isValid = false
        }
        
        if (joinDate.isBlank()) {
            joinDateError = "Join date is required"
            isValid = false
        } else if (!validateJoinDate(joinDate)) {
            joinDateError = "Please enter date in DD/MM/YYYY format"
            isValid = false
        }
        
        if (startTime.isBlank()) {
            startTimeError = "Start time is required"
            isValid = false
        } else if (!validateTime(startTime)) {
            startTimeError = "Please enter a valid hour (0-23)"
            isValid = false
        }
        
        if (endTime.isBlank()) {
            endTimeError = "End time is required"
            isValid = false
        } else if (!validateTime(endTime)) {
            endTimeError = "Please enter a valid hour (0-23)"
            isValid = false
        }
        
        return isValid
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Staff") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Show error message if any
            val errorMessage = uiState.error
            if (errorMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(
                        text = errorMessage,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Name field
            OutlinedTextField(
                value = name,
                onValueChange = { 
                    name = it
                    nameError = ""
                },
                label = { Text("Name *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = nameError.isNotEmpty(),
                supportingText = if (nameError.isNotEmpty()) {
                    { Text(nameError) }
                } else null
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Email field
            OutlinedTextField(
                value = email,
                onValueChange = { 
                    email = it
                    emailError = ""
                },
                label = { Text("Email *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = emailError.isNotEmpty(),
                supportingText = if (emailError.isNotEmpty()) {
                    { Text(emailError) }
                } else null
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Phone field
            OutlinedTextField(
                value = phone,
                onValueChange = { 
                    if (it.all { char -> char.isDigit() }) {
                        phone = it
                        phoneError = ""
                    }
                },
                label = { Text("Phone *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                isError = phoneError.isNotEmpty(),
                supportingText = if (phoneError.isNotEmpty()) {
                    { Text(phoneError) }
                } else null
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Address field
            OutlinedTextField(
                value = address,
                onValueChange = { 
                    address = it
                    addressError = ""
                },
                label = { Text("Address *") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                isError = addressError.isNotEmpty(),
                supportingText = if (addressError.isNotEmpty()) {
                    { Text(addressError) }
                } else null
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Role field - Dropdown
            ExposedDropdownMenuBox(
                expanded = roleDropdownExpanded,
                onExpandedChange = { roleDropdownExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = role,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Role *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleDropdownExpanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    isError = roleError.isNotEmpty(),
                    supportingText = if (roleError.isNotEmpty()) {
                        { Text(roleError) }
                    } else null,
                    placeholder = { Text("Select a role") }
                )
                
                ExposedDropdownMenu(
                    expanded = roleDropdownExpanded,
                    onDismissRequest = { roleDropdownExpanded = false }
                ) {
                    roleOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.replaceFirstChar { it.uppercase() }) },
                            onClick = {
                                role = option
                                roleError = ""
                                roleDropdownExpanded = false
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Salary field
            OutlinedTextField(
                value = salary,
                onValueChange = { 
                    salary = it
                    salaryError = ""
                },
                label = { Text("Salary *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                prefix = { Text("₹ ") },
                isError = salaryError.isNotEmpty(),
                supportingText = if (salaryError.isNotEmpty()) {
                    { Text(salaryError) }
                } else null
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Join Date field
            OutlinedTextField(
                value = joinDate,
                onValueChange = { 
                    // Format as DD/MM/YYYY
                    val filtered = it.filter { char -> char.isDigit() || char == '/' }
                    if (filtered.length <= 10) {
                        joinDate = filtered
                        joinDateError = ""
                    }
                },
                label = { Text("Join Date *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("DD/MM/YYYY") },
                isError = joinDateError.isNotEmpty(),
                supportingText = if (joinDateError.isNotEmpty()) {
                    { Text(joinDateError) }
                } else null
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Shift Timing Section
            Text(
                text = "Shift Timing",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(modifier = Modifier.fillMaxWidth()) {
                // Start Time
                OutlinedTextField(
                    value = startTime,
                    onValueChange = { 
                        if (it.length <= 2 && (it.isEmpty() || it.all { char -> char.isDigit() })) {
                            startTime = it
                            startTimeError = ""
                        }
                    },
                    label = { Text("Start Time *") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    placeholder = { Text("HH (24hr)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = startTimeError.isNotEmpty(),
                    supportingText = if (startTimeError.isNotEmpty()) {
                        { Text(startTimeError) }
                    } else null
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // End Time
                OutlinedTextField(
                    value = endTime,
                    onValueChange = { 
                        if (it.length <= 2 && (it.isEmpty() || it.all { char -> char.isDigit() })) {
                            endTime = it
                            endTimeError = ""
                        }
                    },
                    label = { Text("End Time *") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    placeholder = { Text("HH (24hr)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = endTimeError.isNotEmpty(),
                    supportingText = if (endTimeError.isNotEmpty()) {
                        { Text(endTimeError) }
                    } else null
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Status Section
            Text(
                text = "Status",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Column {
                StatusRadioButton(
                    text = "Active",
                    selected = status == "active",
                    onClick = { status = "active" }
                )
                
                StatusRadioButton(
                    text = "Inactive",
                    selected = status == "inactive",
                    onClick = { status = "inactive" }
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Submit Button
            Button(
                onClick = {
                    if (validateForm()) {
                        staffToEdit?.let { staff ->
                            // Safe conversion with validation
                            val salaryValue = salary.toDoubleOrNull()
                            
                            if (salaryValue != null) {
                                updateInitiated = true
                                viewModel.updateStaff(
                                    staffId = staff.id,
                                    name = name,
                                    email = email,
                                    phone = phone,
                                    mobileNumber = phone,
                                    address = address,
                                    role = role,
                                    salary = salaryValue,
                                    joinDate = joinDate,
                                    startTime = startTime,
                                    endTime = endTime,
                                    status = status
                                )
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading && staffToEdit != null
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Update Staff")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}