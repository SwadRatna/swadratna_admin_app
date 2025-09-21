package com.swadratna.swadratna_admin.ui.analytics

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.swadratna.swadratna_admin.data.model.*
import com.swadratna.swadratna_admin.ui.analytics.components.*

@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedFranchise by viewModel.selectedFranchise.collectAsState()
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
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Admin Analytics",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Franchise Selector
            when (uiState) {
                is AnalyticsUiState.Success -> {
                    val data = (uiState as AnalyticsUiState.Success).data

                    FranchiseSelector(
                        franchises = data.franchises,
                        selectedFranchiseId = selectedFranchise,
                        onFranchiseSelected = { viewModel.selectFranchise(it) }
                    )

                    AnalyticsContent(
                        data = data,
                        selectedFranchiseId = selectedFranchise
                    )
                }

                is AnalyticsUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is AnalyticsUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (uiState as AnalyticsUiState.Error).message,
                            color = Color.Red
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FranchiseSelector(
    franchises: List<Franchise>,
    selectedFranchiseId: String?,
    onFranchiseSelected: (String?) -> Unit
) {
    val allFranchisesOption = Franchise("all", "All Franchises")
    val options = listOf(allFranchisesOption) + franchises
    
    var expanded by remember { mutableStateOf(false) }
    val selectedOption = if (selectedFranchiseId == null) {
        allFranchisesOption
    } else {
        franchises.find { it.id == selectedFranchiseId } ?: allFranchisesOption
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(selectedOption.name)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clickable { expanded = true }
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Dropdown icon"
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.9f),
            shape = RoundedCornerShape(6.dp)
        ) {
            options.forEach { franchise ->
                DropdownMenuItem(
                    text = { Text(franchise.name) },
                    onClick = {
                        onFranchiseSelected(if (franchise.id == "all") null else franchise.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun AnalyticsContent(
    data: AnalyticsData,
    selectedFranchiseId: String?
) {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // Sales and ROI Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricCard(
                title = "Total Sales",
                value = data.totalSales.totalValue,
                percentChange = data.totalSales.percentChange,
                modifier = Modifier.weight(1f)
            )
            
            MetricCard(
                title = "Total ROI",
                value = data.totalROI.totalValue,
                percentChange = data.totalROI.percentChange,
                modifier = Modifier.weight(1f)
            )
        }
        
        // Franchise Sales Data
        if (selectedFranchiseId == null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = MaterialTheme.shapes.medium,
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    data.totalSales.franchiseData.forEach { franchiseData ->
                        FranchiseMetricItem(
                            franchiseName = franchiseData.franchiseName,
                            value = franchiseData.value,
                            percentChange = franchiseData.percentChange
                        )
                    }
                }
            }
        }
        
        // Customer and Order Value Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricCard(
                title = "Customer Acquisition",
                value = data.customerAcquisition.totalValue,
                percentChange = data.customerAcquisition.percentChange,
                modifier = Modifier.weight(1f)
            )
            
            MetricCard(
                title = "Average Order Value",
                value = data.averageOrderValue.totalValue,
                percentChange = data.averageOrderValue.percentChange,
                modifier = Modifier.weight(1f)
            )
        }
        
        // Franchise Customer Data
        if (selectedFranchiseId == null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = MaterialTheme.shapes.medium,
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    data.customerAcquisition.franchiseData.forEach { franchiseData ->
                        FranchiseMetricItem(
                            franchiseName = franchiseData.franchiseName,
                            value = franchiseData.value,
                            percentChange = franchiseData.percentChange
                        )
                    }
                }
            }
        }
        
        // Sales Performance Chart
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = MaterialTheme.shapes.medium,
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sales Performance Comparison",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    TimeFrameSelector(
                        timeFrame = data.salesPerformance.timeFrame,
                        onTimeFrameSelected = { /* Handle time frame selection */ }
                    )
                }
                
                val performanceData = if (selectedFranchiseId != null) {
                    data.salesPerformance.franchisePerformance
                        .find { it.franchiseId == selectedFranchiseId }
                        ?.performanceData ?: emptyList()
                } else {
                    data.salesPerformance.franchisePerformance.firstOrNull()?.performanceData ?: emptyList()
                }
                
                LineChart(
                    data = performanceData,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
        
        // Monthly Order Volume
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = MaterialTheme.shapes.medium,
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Monthly Order Volume",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    TimeFrameSelector(
                        timeFrame = data.monthlyOrderVolume.timeFrame,
                        onTimeFrameSelected = { /* Handle time frame selection */ }
                    )
                }
                
                BarChart(
                    data = data.monthlyOrderVolume.volumeData,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
        
        // Top Selling Product Categories
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = MaterialTheme.shapes.medium,
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Top Selling Product Categories",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    TimeFrameSelector(
                        timeFrame = data.topSellingProductCategories.timeFrame,
                        onTimeFrameSelected = { /* Handle time frame selection */ }
                    )
                }
                
                PieChart(
                    categories = data.topSellingProductCategories.categories,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                )
            }
        }
        
        // Export Report Button
        Button(
            onClick = { /* Handle export report */ },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Export Report")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}