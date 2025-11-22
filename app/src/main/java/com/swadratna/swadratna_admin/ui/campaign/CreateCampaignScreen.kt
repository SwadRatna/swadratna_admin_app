package com.swadratna.swadratna_admin.ui.campaign

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.swadratna.swadratna_admin.R
import com.swadratna.swadratna_admin.data.model.CampaignStatus
import com.swadratna.swadratna_admin.ui.menu.MenuCategoriesUiState
import com.swadratna.swadratna_admin.ui.menu.MenuManagementViewModel
import com.swadratna.swadratna_admin.ui.store.StoreViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCampaignScreen(
    onNavigateBack: () -> Unit = {},
    campaignId: String? = null,
    viewModel: CampaignViewModel = hiltViewModel(),
    navController: NavController
) {
    LaunchedEffect(campaignId) {
        if (campaignId != null) {
            viewModel.handleEvent(CampaignEvent.EditCampaign(campaignId))
        }
    }

    val uiState by viewModel.uiState.collectAsState()
    val isEditMode = uiState.isEditMode
    val campaignToEdit = uiState.campaignToEdit
    
    // Debug logging for campaignToEdit changes
    LaunchedEffect(campaignToEdit) {
        android.util.Log.d("CreateCampaignScreen", "campaignToEdit changed: ${campaignToEdit?.id}, youtubeVideoUrl: ${campaignToEdit?.youtubeVideoUrl}")
    }

    var selectedStatus by remember { mutableStateOf(campaignToEdit?.status) }
    LaunchedEffect(campaignToEdit?.status) { selectedStatus = campaignToEdit?.status }

    var statusChangeInitiated by remember { mutableStateOf(false) }

    var navigateAfterSave by remember { mutableStateOf(false) }
    val storeViewModel: StoreViewModel = hiltViewModel()
    val storeUiState by storeViewModel.uiState.collectAsState()

    val menuViewModel: MenuManagementViewModel = hiltViewModel()
    val categoriesState by menuViewModel.categoriesState.collectAsState()
    var selectedCategoryIds by remember { mutableStateOf<Set<Int>>(emptySet()) }
    LaunchedEffect(Unit) {
        menuViewModel.loadCategories()
        // Debug log to check initial state
        android.util.Log.d("CreateCampaignScreen", "Initial campaignToEdit: ${campaignToEdit?.youtubeVideoUrl}")
    }
    
    var campaignTitle by remember { mutableStateOf(campaignToEdit?.title ?: "") }
    var campaignDescription by remember { mutableStateOf(campaignToEdit?.description ?: "") }
    var youtubeVideoUrl by remember { mutableStateOf("") }
    var expandedFranchiseDropdown by remember { mutableStateOf(false) }
    var selectedStoreId by remember { mutableStateOf<Int?>(null) }
    var selectedStoreName by remember { mutableStateOf("All Stores") }

    var startDate by remember { mutableStateOf(campaignToEdit?.startDate) }
    var endDate by remember { mutableStateOf(campaignToEdit?.endDate) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd MMM yyyy") }

    LaunchedEffect(campaignToEdit, storeUiState.stores) {
        android.util.Log.d("CreateCampaignScreen", "LaunchedEffect triggered with campaignToEdit: ${campaignToEdit?.id}, youtubeVideoUrl: ${campaignToEdit?.youtubeVideoUrl}")
        if (isEditMode && campaignToEdit != null) {
            campaignTitle = campaignToEdit.title
            campaignDescription = campaignToEdit.description
            youtubeVideoUrl = campaignToEdit.youtubeVideoUrl ?: ""
            startDate = campaignToEdit.startDate
            endDate = campaignToEdit.endDate
            selectedCategoryIds = campaignToEdit.targetCategoryIds.toSet()

            val ids = campaignToEdit.targetFranchiseIds
            if (ids.isEmpty()) {
                selectedStoreId = null
                selectedStoreName = "All Stores"
            } else {
                selectedStoreId = ids.firstOrNull()
                selectedStoreName = storeUiState.stores.find { it.id == selectedStoreId }?.name
                    ?: ("Store #${selectedStoreId}")
            }
            android.util.Log.d("CreateCampaignScreen", "Updated youtubeVideoUrl to: '$youtubeVideoUrl'")
        }
    }

    // Removed redundant LaunchedEffect - youtubeVideoUrl is already handled in the main LaunchedEffect above

    val scroll = rememberScrollState()

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
            title = { Text(if (isEditMode) "Edit Campaign" else "Create Campaign") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack, enabled = !uiState.isLoading) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            windowInsets = WindowInsets(0.dp)
        )

        LaunchedEffect(uiState.isLoading, uiState.error, statusChangeInitiated) {
            if (statusChangeInitiated && !uiState.isLoading && uiState.error == null) {
                statusChangeInitiated = false
                onNavigateBack()
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp, bottom = 24.dp)
        ) {
            if (isEditMode && campaignToEdit != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            selectedStatus = CampaignStatus.ACTIVE
                            statusChangeInitiated = true
                            viewModel.handleEvent(
                                CampaignEvent.UpdateCampaignStatus(campaignToEdit.id, CampaignStatus.ACTIVE)
                            )
                        },
                        enabled = !uiState.isLoading,
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (selectedStatus == CampaignStatus.ACTIVE) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent,
                            contentColor = if (selectedStatus == CampaignStatus.ACTIVE) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        ),
                        border = BorderStroke(1.dp, if (selectedStatus == CampaignStatus.ACTIVE) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
                    ) { Text("Enable") }

                    OutlinedButton(
                        onClick = {
                            selectedStatus = CampaignStatus.COMPLETED
                            statusChangeInitiated = true
                            viewModel.handleEvent(
                                CampaignEvent.UpdateCampaignStatus(campaignToEdit.id, CampaignStatus.COMPLETED)
                            )
                        },
                        enabled = !uiState.isLoading,
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (selectedStatus == CampaignStatus.COMPLETED) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent,
                            contentColor = if (selectedStatus == CampaignStatus.COMPLETED) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        ),
                        border = BorderStroke(1.dp, if (selectedStatus == CampaignStatus.COMPLETED) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
                    ) { Text("Disable") }
                }
                Spacer(Modifier.height(16.dp))
            }

            Text("Campaign Title*", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = campaignTitle,
                onValueChange = { campaignTitle = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Diwali Mega Offer") },
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                isError = campaignTitle.isEmpty() && campaignTitle.isNotBlank(),
                supportingText = { 
                    if (campaignTitle.isEmpty() && campaignTitle.isNotBlank()) {
                        Text("Title is required")
                    }
                }
            )

            Spacer(Modifier.height(16.dp))

            Text("Campaign Duration*", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
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
                        Icon(painter = painterResource(R.drawable.ic_date), contentDescription = "Select date")
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
                        .clickable { if (startDate != null) showEndDatePicker = true },
                    placeholder = { Text("End Date") },
                    trailingIcon = { IconButton(onClick = { if (startDate != null) showEndDatePicker = true }) {
                        Icon(painter = painterResource(R.drawable.ic_date), contentDescription = "Select Date",
                             tint = if (startDate == null) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f) 
                                   else MaterialTheme.colorScheme.onSurfaceVariant)
                    } },
                    readOnly = true,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = MaterialTheme.typography.bodyMedium.fontSize
                    ),
                    shape = RoundedCornerShape(8.dp),
                    enabled = startDate != null
                )
            }

            if (showStartDatePicker) {
                val startState = rememberDatePickerState(
                    initialSelectedDateMillis = System.currentTimeMillis()
                )
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

            if (showEndDatePicker && startDate != null) {
                val startMillis = startDate!!.toEpochDay() * (24 * 60 * 60 * 1000)
                val initial = endDate?.toEpochDay()?.times(24 * 60 * 60 * 1000) ?: startMillis
                val endState = rememberDatePickerState(
                    initialSelectedDateMillis = initial
                )
                DatePickerDialog(
                    onDismissRequest = { showEndDatePicker = false },
                    confirmButton = {
                        Button(onClick = {
                            val picked = endState.selectedDateMillis
                            if (picked != null && picked >= startMillis) {
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

            Text("Campaign Description*", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = campaignDescription,
                onValueChange = { campaignDescription = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Enjoy 15% off this festive season.") },
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                isError = campaignDescription.isEmpty() && campaignDescription.isNotBlank(),
                supportingText = { 
                    if (campaignDescription.isEmpty() && campaignDescription.isNotBlank()) {
                        Text("Description is required")
                    }
                }
            )

            Spacer(Modifier.height(16.dp))

            Text("Select Target Franchises*", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))
            ExposedDropdownMenuBox(
                expanded = expandedFranchiseDropdown,
                onExpandedChange = { expandedFranchiseDropdown = it }
            ) {
                OutlinedTextField(
                    value = selectedStoreName,
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
                    DropdownMenuItem(
                        text = { Text("All Stores") },
                        onClick = {
                            selectedStoreId = null
                            selectedStoreName = "All Stores"
                            expandedFranchiseDropdown = false
                        }
                    )
                    storeUiState.stores.forEach { store ->
                        DropdownMenuItem(
                            text = { Text(store.name) },
                            onClick = {
                                selectedStoreId = store.id
                                selectedStoreName = store.name
                                expandedFranchiseDropdown = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Text("Select Menu Category*", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))
            when (categoriesState) {
                is MenuCategoriesUiState.Loading -> {
                    Text("Loading categories...")
                }
                is MenuCategoriesUiState.Error -> {
                    val msg = (categoriesState as MenuCategoriesUiState.Error).message
                    Text(msg)
                }
                is MenuCategoriesUiState.Success -> {
                    val categories = (categoriesState as MenuCategoriesUiState.Success).categories
                    Column(modifier = Modifier.fillMaxWidth()) {
                        categories.forEach { cat ->
                            val id = cat.id
                            if (id != null) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = selectedCategoryIds.contains(id),
                                        onCheckedChange = {
                                            selectedCategoryIds = if (it) selectedCategoryIds + id else selectedCategoryIds - id
                                        }
                                    )
                                    Text(cat.name)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            
            Text("YouTube Video URL (Optional)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))
            android.util.Log.d("CreateCampaignScreen", "Rendering YouTube field with value: '$youtubeVideoUrl'")
            OutlinedTextField(
                value = youtubeVideoUrl,
                onValueChange = { youtubeVideoUrl = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Write youtube link here") },
                singleLine = true,
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(Modifier.height(16.dp))
            
            Text("Upload Image (Optional)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))
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
                        painter = painterResource(R.drawable.ic_upload),
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
                    val targetCategoryIds = selectedCategoryIds.toList()
                    val targetFranchiseIds = selectedStoreId?.let { listOf(it) } ?: emptyList()
                    if (isEditMode && campaignToEdit != null) {
                        viewModel.handleEvent(
                            CampaignEvent.UpdateCampaign(
                                id = campaignToEdit.id,
                                title = campaignTitle,
                                description = campaignDescription,
                                startDate = startDate!!,
                                endDate = endDate!!,
                                type = campaignToEdit.type,
                                discount = campaignToEdit.discount,
                                targetCategoryIds = targetCategoryIds,
                                targetFranchiseIds = targetFranchiseIds,
                                imageUrl = campaignToEdit.imageUrl,
                                youtubeVideoUrl = youtubeVideoUrl
                            )
                        )
                    } else {
                        viewModel.handleEvent(
                            CampaignEvent.CreateCampaign(
                                title = campaignTitle,
                                description = campaignDescription,
                                startDate = startDate!!,
                                endDate = endDate!!,
                                targetCategoryIds = targetCategoryIds,
                                targetFranchiseIds = targetFranchiseIds,
                                imageUrl = null,
                                youtubeVideoUrl = youtubeVideoUrl
                            )
                        )
                    }
                    navigateAfterSave = true
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = campaignTitle.isNotBlank() && campaignDescription.isNotBlank()
                        && startDate != null && endDate != null
                        && selectedCategoryIds.isNotEmpty()
                        && !uiState.isLoading
            ) { Text(if (isEditMode) "Save Campaign" else "Create Campaign") }
            }

            Spacer(Modifier.height(24.dp))
        }

        if (navigateAfterSave && !uiState.isLoading && uiState.error == null) {
            navController.previousBackStackEntry
                ?.savedStateHandle
                ?.set("refreshCampaigns", true)

            onNavigateBack()
            navigateAfterSave = false
        }
    }
}
