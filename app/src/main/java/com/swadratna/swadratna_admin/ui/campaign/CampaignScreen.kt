package com.swadratna.swadratna_admin.ui.campaign

import androidx.compose.foundation.clickable
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.swadratna.swadratna_admin.R
import com.swadratna.swadratna_admin.ui.campaign.components.CampaignItem
import com.swadratna.swadratna_admin.ui.components.AppSearchField
import com.swadratna.swadratna_admin.ui.staff.FilterOption

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CampaignScreen(
    viewModel: CampaignViewModel = hiltViewModel(),
    onNavigateToDetails: (String) -> Unit = {},
    onNavigateToCreateCampaign: () -> Unit = {},
    onNavigateToEditCampaign: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp)
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .consumeWindowInsets(inner)
                .padding(16.dp)
                .statusBarsPadding()
        ) {
            Text(
                text = "Campaign",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(16.dp))

            AppSearchField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.handleEvent(CampaignEvent.SearchQueryChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = "Search campaigns...",
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.handleEvent(CampaignEvent.ToggleFilterMenu) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(painter = painterResource(R.drawable.ic_filter), contentDescription = "Filter")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Filter")
                }

                OutlinedButton(
                    onClick = { viewModel.handleEvent(CampaignEvent.ToggleSortMenu) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(painter = painterResource(R.drawable.ic_sort), contentDescription = "Sort")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Sort")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onNavigateToCreateCampaign,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create New Campaign")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.fillMaxSize()) {
                if (uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (uiState.campaigns.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No campaigns found")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(uiState.campaigns) { campaign ->
                            CampaignItem(
                                campaign = campaign,
                                onViewDetails = onNavigateToDetails,
                                onEdit = {
                                    viewModel.handleEvent(CampaignEvent.EditCampaign(it))
                                    onNavigateToEditCampaign(it)
                                },
                                onDelete = { viewModel.handleEvent(CampaignEvent.DeleteCampaign(it)) }
                            )
                        }
                    }
                }

                if (uiState.isFilterMenuVisible) {
                    FilterMenus(
                        selectedFilter = uiState.filter,
                        onFilterSelected = { viewModel.handleEvent(CampaignEvent.FilterChanged(it)) },
                        onDismiss = { viewModel.handleEvent(CampaignEvent.ToggleFilterMenu) },
                        modifier = Modifier.align(Alignment.TopEnd)
                    )
                }

                if (uiState.isSortMenuVisible) {
                    SortMenus(
                        selectedSortOrder = uiState.sortOrder,
                        onSortOrderSelected = { viewModel.handleEvent(CampaignEvent.SortChanged(it)) },
                        onDismiss = { viewModel.handleEvent(CampaignEvent.ToggleSortMenu) },
                        modifier = Modifier.align(Alignment.TopEnd)
                    )
                }
            }
        }
    }
}

@Composable
fun FilterMenus(
    selectedFilter: CampaignFilter,
    onFilterSelected: (CampaignFilter) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 3.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Filter by Status",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            FilterOption(
                text = "All",
                isSelected = selectedFilter == CampaignFilter.ALL,
                onClick = { onFilterSelected(CampaignFilter.ALL); onDismiss() }
            )

            FilterOption(
                text = "Active",
                isSelected = selectedFilter == CampaignFilter.ACTIVE,
                onClick = { onFilterSelected(CampaignFilter.ACTIVE); onDismiss() }
            )

            FilterOption(
                text = "Scheduled",
                isSelected = selectedFilter == CampaignFilter.SCHEDULED,
                onClick = { onFilterSelected(CampaignFilter.SCHEDULED); onDismiss() }
            )

            FilterOption(
                text = "Completed",
                isSelected = selectedFilter == CampaignFilter.COMPLETED,
                onClick = { onFilterSelected(CampaignFilter.COMPLETED); onDismiss() }
            )

            FilterOption(
                text = "Draft",
                isSelected = selectedFilter == CampaignFilter.DRAFT,
                onClick = { onFilterSelected(CampaignFilter.DRAFT); onDismiss() }
            )
        }
    }
}

@Composable
fun SortMenus(
    selectedSortOrder: CampaignSortOrder,
    onSortOrderSelected: (CampaignSortOrder) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 3.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Sort by",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            FilterOption(
                text = "Title A-Z",
                isSelected = selectedSortOrder == CampaignSortOrder.TITLE_ASC,
                onClick = { onSortOrderSelected(CampaignSortOrder.TITLE_ASC); onDismiss() }
            )

            FilterOption(
                text = "Title Z-A",
                isSelected = selectedSortOrder == CampaignSortOrder.TITLE_DESC,
                onClick = { onSortOrderSelected(CampaignSortOrder.TITLE_DESC); onDismiss() }
            )

            FilterOption(
                text = "Date Newest first",
                isSelected = selectedSortOrder == CampaignSortOrder.DATE_DESC,
                onClick = { onSortOrderSelected(CampaignSortOrder.DATE_DESC); onDismiss() }
            )

            FilterOption(
                text = "Date Oldest first",
                isSelected = selectedSortOrder == CampaignSortOrder.DATE_ASC,
                onClick = { onSortOrderSelected(CampaignSortOrder.DATE_ASC); onDismiss() }
            )
        }
    }
}

@Composable
fun SortMenu(
    selectedSortOrder: CampaignSortOrder,
    onSortOrderSelected: (CampaignSortOrder) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 3.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Sort by",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            FilterOption(
                text = "Title (A-Z)",
                isSelected = selectedSortOrder == CampaignSortOrder.TITLE_ASC,
                onClick = { onSortOrderSelected(CampaignSortOrder.TITLE_ASC); onDismiss() }
            )

            FilterOption(
                text = "Title (Z-A)",
                isSelected = selectedSortOrder == CampaignSortOrder.TITLE_DESC,
                onClick = { onSortOrderSelected(CampaignSortOrder.TITLE_DESC); onDismiss() }
            )

            FilterOption(
                text = "Date (Newest first)",
                isSelected = selectedSortOrder == CampaignSortOrder.DATE_DESC,
                onClick = { onSortOrderSelected(CampaignSortOrder.DATE_DESC); onDismiss() }
            )

            FilterOption(
                text = "Date (Oldest first)",
                isSelected = selectedSortOrder == CampaignSortOrder.DATE_ASC,
                onClick = { onSortOrderSelected(CampaignSortOrder.DATE_ASC); onDismiss() }
            )
        }
    }
}

@Composable
fun FilterOption(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}