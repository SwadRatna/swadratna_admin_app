package com.swadratna.swadratna_admin.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverallReportScreen(
    onNavigateBack: () -> Unit,
    viewModel: OverallReportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker by remember { mutableStateOf(false) }
    var extraName by remember { mutableStateOf("") }
    var extraValue by remember { mutableStateOf("") }
    var extraOperation by remember { mutableStateOf("add") }

    if (showFromPicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showFromPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = java.time.Instant.ofEpochMilli(millis)
                            .atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                        viewModel.setDateRange(date.toString(), uiState.toDate)
                    }
                    showFromPicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showFromPicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = datePickerState) }
    }

    if (showToPicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showToPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = java.time.Instant.ofEpochMilli(millis)
                            .atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                        viewModel.setDateRange(uiState.fromDate, date.toString())
                    }
                    showToPicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showToPicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = datePickerState) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Overall Report") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                actions = { }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = { showFromPicker = true }
                    ) {
                        AutoResizeText("From: ${uiState.fromDate}")
                    }

                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = { showToPicker = true }
                    ) {
                        AutoResizeText("To: ${uiState.toDate}")
                    }
                }
            }

            item {
                Card { 
                    Column(Modifier.padding(16.dp)) {
                        Text("Totals", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Sales")
                            Text("₹${String.format("%.2f", uiState.salesTotal)}")
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Salary")
                            Text("₹${String.format("%.2f", uiState.salaryTotal)}")
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Inventory Expenditure")
                            Text("₹${String.format("%.2f", uiState.inventoryTotal)}")
                        }
                    }
                }
            }

            item {
                Card { 
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Extras", fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = extraName, onValueChange = { extraName = it }, label = { Text("Name") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = extraValue, onValueChange = { extraValue = it }, label = { Text("Value") }, modifier = Modifier.weight(1f))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(selected = extraOperation == "add", onClick = { extraOperation = "add" }, label = { Text("Add") })
                            FilterChip(selected = extraOperation == "subtract", onClick = { extraOperation = "subtract" }, label = { Text("Subtract") })
                            Button(onClick = {
                                val v = extraValue.toDoubleOrNull() ?: 0.0
                                if (extraName.isNotBlank() && v > 0.0) {
                                    viewModel.addExtra(extraName, v, extraOperation)
                                    extraName = ""
                                    extraValue = ""
                                    extraOperation = "add"
                                }
                            }) { Text("Add Extra") }
                        }
                        if (uiState.extras.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            uiState.extras.forEachIndexed { index, item ->
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("${item.name} (${item.operation})")
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("₹${String.format("%.2f", item.value)}")
                                        TextButton(onClick = { viewModel.removeExtra(index) }) { Text("Remove") }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Card { 
                    Column(Modifier.padding(16.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Final Earning", fontWeight = FontWeight.Bold)
                            Text("₹${String.format("%.2f", uiState.finalEarning)}", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AutoResizeText(
    text: String,
    modifier: Modifier = Modifier,
    maxFontSizeSp: Float = 16f,
    minFontSizeSp: Float = 10f,
    stepSp: Float = 1f
) {
    var fontSizeSp by remember { mutableStateOf(maxFontSizeSp) }

    Text(
        text = text,
        modifier = modifier,
        maxLines = 1,
        softWrap = false,
        fontSize = fontSizeSp.sp,
        onTextLayout = { result ->
            if (result.hasVisualOverflow && fontSizeSp > minFontSizeSp) {
                fontSizeSp -= stepSp
            }
        }
    )
}

