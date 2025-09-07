package com.swadratna.swadratna_admin.ui.campaign

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCampaignScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: CampaignViewModel = hiltViewModel()
) {
    var campaignTitle by remember { mutableStateOf("") }            // UI state hoisted in composable [2]
    var campaignDescription by remember { mutableStateOf("") }      // [2]
    var selectedFranchises by remember { mutableStateOf("All") }    // [2]
    var expandedFranchiseDropdown by remember { mutableStateOf(false) } // [2]

    var startDate by remember { mutableStateOf<LocalDate?>(null) }  // [2]
    var endDate by remember { mutableStateOf<LocalDate?>(null) }    // [2]
    var showStartDatePicker by remember { mutableStateOf(false) }   // 
    var showEndDatePicker by remember { mutableStateOf(false) }     // 
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd MMM yyyy") } // 

    var snacksChecked by remember { mutableStateOf(false) }         // [2]
    var southIndianChecked by remember { mutableStateOf(false) }    // [2]
    var vegGravyChecked by remember { mutableStateOf(false) }       // [2]
    var chineseChecked by remember { mutableStateOf(false) }        // [2]
    var nonVegChecked by remember { mutableStateOf(false) }         // [2]

    val scroll = rememberScrollState()                              // 

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(
                WindowInsets.safeDrawing.only(
                    WindowInsetsSides.Top + WindowInsetsSides.Bottom
                )
            )
    ) {
        TopAppBar(
            title = { Text("Create Campaign") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            windowInsets = WindowInsets(0.dp) // parent provides top inset [3]
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp, bottom = 24.dp)
        ) {
            Text("Campaign Title", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = campaignTitle,
                onValueChange = { campaignTitle = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Diwali Mega Offer") },
                singleLine = true,
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(Modifier.height(16.dp))


            Text("Campaign Duration", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = startDate?.format(dateFormatter) ?: "" ,
                    onValueChange = {},
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showStartDatePicker = true },
                    placeholder = { Text("Start Date") },
                    trailingIcon = { IconButton(onClick = { showStartDatePicker = true }) {
                        Icon(Icons.Default.Home, contentDescription = "Select date")
                    } },
                    readOnly = true,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = MaterialTheme.typography.bodyMedium.fontSize
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                OutlinedTextField(
                    value = endDate?.format(dateFormatter) ?: "",
                    onValueChange = {},
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showEndDatePicker = true },
                    placeholder = { Text("End Date") },
                    trailingIcon = { IconButton(onClick = { showEndDatePicker = true }) {
                        Icon(Icons.Default.Home, contentDescription = "Select Date")
                    } },
                    readOnly = true,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = MaterialTheme.typography.bodyMedium.fontSize
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            if (showStartDatePicker) {
                val startState = rememberDatePickerState(
                    initialSelectedDateMillis = System.currentTimeMillis()
                ) // must be created in a composable scope
                DatePickerDialog(
                    onDismissRequest = { showStartDatePicker = false },
                    confirmButton = {
                        Button(onClick = {
                            startState.selectedDateMillis?.let { millis ->
                                startDate = LocalDate.ofEpochDay(millis / (24 * 60 * 60 * 1000))
                            }
                            showStartDatePicker = false
                        }) { Text("OK") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showStartDatePicker = false }) { Text("Cancel") }
                    }
                ) {
                    DatePicker(
                        state = startState,
                        showModeToggle = false
                    )
                }
            }

            if (showEndDatePicker) {
                val initial = startDate?.toEpochDay()?.times(24 * 60 * 60 * 1000)
                    ?: System.currentTimeMillis()
                val endState = rememberDatePickerState(
                    initialSelectedDateMillis = initial
                )
                DatePickerDialog(
                    onDismissRequest = { showEndDatePicker = false },
                    confirmButton = {
                        Button(onClick = {
                            val picked = endState.selectedDateMillis
                            val startMillis = startDate?.toEpochDay()?.times(24 * 60 * 60 * 1000)
                            if (picked != null && (startMillis == null || picked >= startMillis)) {
                                endDate = LocalDate.ofEpochDay(picked / (24 * 60 * 60 * 1000))
                                showEndDatePicker = false
                            } else {
                               // TODO: Handle with snackbar
                            }
                        }) { Text("OK") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showEndDatePicker = false }) { Text("Cancel") }
                    }
                ) {
                    DatePicker(
                        state = endState,
                        showModeToggle = false,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text("Campaign Description", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = campaignDescription,
                onValueChange = { campaignDescription = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Enjoy 15% off this festive season.") },
                singleLine = true,
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(Modifier.height(16.dp))

            Text("Select Target Franchises", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))
            ExposedDropdownMenuBox(
                expanded = expandedFranchiseDropdown,
                onExpandedChange = { expandedFranchiseDropdown = it }
            ) {
                OutlinedTextField(
                    value = selectedFranchises,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFranchiseDropdown) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(8.dp)
                )

                ExposedDropdownMenu(
                    expanded = expandedFranchiseDropdown,
                    onDismissRequest = { expandedFranchiseDropdown = false }
                ) {
                    listOf("All", "North Region", "South Region", "East Region", "West Region").forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                selectedFranchises = option
                                expandedFranchiseDropdown = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Text("Select Menu Category", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = snacksChecked, onCheckedChange = { snacksChecked = it })
                        Text("Snacks")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = vegGravyChecked, onCheckedChange = { vegGravyChecked = it })
                        Text("Veg Gravy")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = nonVegChecked, onCheckedChange = { nonVegChecked = it })
                        Text("Non Veg")
                    }
                }
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = southIndianChecked, onCheckedChange = { southIndianChecked = it })
                        Text("South Indian")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = chineseChecked, onCheckedChange = { chineseChecked = it })
                        Text("Chinese")
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .clickable { /* TODO */ },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Upload Image",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Tap to upload the offer image.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Cancel") }

                Button(
                    onClick = {
                        val selectedCategories = buildList {
                            if (snacksChecked) add("Snacks")
                            if (southIndianChecked) add("South Indian")
                            if (vegGravyChecked) add("Veg Gravy")
                            if (chineseChecked) add("Chinese")
                            if (nonVegChecked) add("Non Veg")
                        }
                        if (campaignTitle.isNotBlank() && campaignDescription.isNotBlank()
                            && startDate != null && endDate != null
                        ) {
                            viewModel.handleEvent(
                                CampaignEvent.CreateCampaign(
                                    title = campaignTitle,
                                    description = campaignDescription,
                                    startDate = startDate!!,
                                    endDate = endDate!!,
                                    menuCategories = emptyList(),
                                    targetFranchises = "abc",
                                    imageUrl = null
                                )
                            )
                            onNavigateBack()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = campaignTitle.isNotBlank() && campaignDescription.isNotBlank()
                            && startDate != null && endDate != null
                ) { Text("Create Campaign") }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

