package com.swadratna.swadratna_admin.ui.campaign

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.compose.rememberNavController
import com.swadratna.swadratna_admin.R
import com.swadratna.swadratna_admin.ui.campaign.components.CampaignItem
import com.swadratna.swadratna_admin.ui.components.AppSearchField
// Using local CampaignFilterOption function instead of importing

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CampaignScreen(
    viewModel: CampaignViewModel = hiltViewModel(),
    onNavigateToDetails: (String) -> Unit = {},
    onNavigateToCreateCampaign: () -> Unit = {},
    onNavigateToEditCampaign: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val navController = rememberNavController()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        navController.currentBackStackEntryFlow.collect { backStackEntry ->
            val savedStateHandle = backStackEntry.savedStateHandle
            val shouldRefresh = savedStateHandle.get<Boolean>("refreshCampaigns") ?: false
            if (shouldRefresh) {
                viewModel.handleEvent(CampaignEvent.LoadCampaigns)
                savedStateHandle["refreshCampaigns"] = false
            }
        }
    }

    // Refresh campaigns whenever this screen resumes
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.handleEvent(CampaignEvent.RefreshData)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }



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
                    Icon(
                        painter = painterResource(R.drawable.ic_filter),
                        contentDescription = "Filter"
                    )
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
                onClick = onNavigateToCreateCampaign, modifier = Modifier.fillMaxWidth()
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
                        items(uiState.campaigns, key = { it.id }) { campaign ->
                            CampaignItem(
                                campaign = campaign,
                                onEdit = {
                                    viewModel.handleEvent(CampaignEvent.EditCampaign(it))
                                    onNavigateToEditCampaign(it)
                                },
                                onDelete = { viewModel.handleEvent(CampaignEvent.DeleteCampaign(it)) },
                                onChangeStatus = { id, status ->
                                    viewModel.handleEvent(
                                        CampaignEvent.UpdateCampaignStatus(
                                            id, status
                                        )
                                    )
                                })
                        }
                    }
                }

                if (uiState.isFilterMenuVisible) {
                    CampaignFilterMenus(
                        selectedFilter = uiState.filter,
                        onFilterSelected = { viewModel.handleEvent(CampaignEvent.FilterChanged(it)) },
                        onDismiss = { viewModel.handleEvent(CampaignEvent.ToggleFilterMenu) },
                        modifier = Modifier.align(Alignment.TopEnd)
                    )
                }
                
                if (uiState.isSortMenuVisible) {
                    CampaignSortMenus(
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
fun CampaignFilterMenus(
    selectedFilter: CampaignFilter,
    onFilterSelected: (CampaignFilter) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier, shape = MaterialTheme.shapes.medium, tonalElevation = 3.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Filter by Status",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            CampaignFilterOption(
                text = "All",
                isSelected = selectedFilter == CampaignFilter.ALL,
                onClick = { onFilterSelected(CampaignFilter.ALL); onDismiss() })

            CampaignFilterOption(
                text = "Active",
                isSelected = selectedFilter == CampaignFilter.ACTIVE,
                onClick = { onFilterSelected(CampaignFilter.ACTIVE); onDismiss() })

            CampaignFilterOption(
                text = "Scheduled",
                isSelected = selectedFilter == CampaignFilter.SCHEDULED,
                onClick = { onFilterSelected(CampaignFilter.SCHEDULED); onDismiss() })

            CampaignFilterOption(
                text = "Completed",
                isSelected = selectedFilter == CampaignFilter.COMPLETED,
                onClick = { onFilterSelected(CampaignFilter.COMPLETED); onDismiss() })

            CampaignFilterOption(
                text = "Draft",
                isSelected = selectedFilter == CampaignFilter.DRAFT,
                onClick = { onFilterSelected(CampaignFilter.DRAFT); onDismiss() })
        }
    }
}

@Composable
fun CampaignSortMenus(
    selectedSortOrder: CampaignSortOrder,
    onSortOrderSelected: (CampaignSortOrder) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier, shape = MaterialTheme.shapes.medium, tonalElevation = 3.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Sort by",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            CampaignFilterOption(
                text = "Title A-Z",
                isSelected = selectedSortOrder == CampaignSortOrder.TITLE_ASC,
                onClick = { onSortOrderSelected(CampaignSortOrder.TITLE_ASC); onDismiss() })

            CampaignFilterOption(
                text = "Title Z-A",
                isSelected = selectedSortOrder == CampaignSortOrder.TITLE_DESC,
                onClick = { onSortOrderSelected(CampaignSortOrder.TITLE_DESC); onDismiss() })

            CampaignFilterOption(
                text = "Date Newest first",
                isSelected = selectedSortOrder == CampaignSortOrder.DATE_DESC,
                onClick = { onSortOrderSelected(CampaignSortOrder.DATE_DESC); onDismiss() })

            CampaignFilterOption(
                text = "Date Oldest first",
                isSelected = selectedSortOrder == CampaignSortOrder.DATE_ASC,
                onClick = { onSortOrderSelected(CampaignSortOrder.DATE_ASC); onDismiss() })
        }
    }
}

@Composable
fun CampaignFilterOption(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        selected = isSelected,
        onClick = onClick,
        shape = MaterialTheme.shapes.small,
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}