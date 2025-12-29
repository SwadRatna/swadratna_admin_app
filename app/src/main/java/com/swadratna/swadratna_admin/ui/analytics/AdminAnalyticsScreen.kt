package com.swadratna.swadratna_admin.ui.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.swadratna.swadratna_admin.data.model.CardRow
import com.swadratna.swadratna_admin.data.model.Cards

import androidx.compose.ui.graphics.toArgb

import com.swadratna.swadratna_admin.data.model.SalesInfoItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAnalyticsScreen(
    viewModel: AnalyticsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val textColor = MaterialTheme.colorScheme.onSurface.toArgb()
    var showSalesDialog by remember { mutableStateOf(false) }

    if (showSalesDialog) {
        SalesDetailsDialog(
            salesInfo = state.salesInfo,
            onDismiss = { showSalesDialog = false },
            textColor = textColor
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analytics Panel") },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
            )
        },
        contentWindowInsets = WindowInsets(0.dp)
    ) { paddingValues ->
        when {
            state.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            state.error != null -> Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Error: ${state.error}")
            }

            else -> state.analytics?.let { data ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    CardsGrid(
                        cards = data.cards,
                        salesInfo = state.salesInfo,
                        onSalesCardClick = { showSalesDialog = true }
                    )

//                    Text(
//                        "Sales Performance Comparison",
//                        style = MaterialTheme.typography.titleMedium
//                    )
//                    LineChartView(
//                        series = data.salesPerformance,
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .height(220.dp)
//                            .padding(top = 8.dp)
//                    )

                    Spacer(Modifier.height(16.dp))
                    Text("Monthly Order Volume", style = MaterialTheme.typography.titleMedium)
                    GroupedBarChartView(
                        months = data.monthlyOrderVolume,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .padding(top = 8.dp),
                        textColor = textColor
                    )

                    Spacer(Modifier.height(16.dp))
                    Row(Modifier.fillMaxWidth()) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "Top Selling Product Categories",
                                style = MaterialTheme.typography.titleMedium
                            )
                            DonutChartView(
                                categories = data.topProductCategories,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                textColor = textColor
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    if (state.salesInfo.isNotEmpty()) {
//                        Text(
//                            "Item Sales Quantity (25-12-2025)",
//                            style = MaterialTheme.typography.titleMedium
//                        )
//                        Spacer(Modifier.height(8.dp))
//                        SalesInfoTable(state.salesInfo, textColor)
//                        Spacer(Modifier.height(16.dp))
                    }

                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = {
                            val data = state.analytics ?: return@Button
                            val uri = AnalyticsCsvExporter.exportToCsv(
                                context = context,
                                franchise = state.franchiseFilter,
                                analytics = data
                            )
                            val share = androidx.core.app.ShareCompat.IntentBuilder(context)
                                .setType("text/csv")
                                .setStream(uri)
                                .setChooserTitle("Share analytics CSV")
                                .createChooserIntent()
                                .addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            context.startActivity(share)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Export Report") }
                }
            }
        }
    }
}

//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//private fun StoresDropdown(
//    selectedFranchise: String?,
//    availableFranchises: List<String>,
//    onFranchiseChange: (String?) -> Unit
//) {
//    var expanded by remember { mutableStateOf(false) }
//    val selectedText = selectedFranchise ?: "Select franchise"
//
//    ExposedDropdownMenuBox(
//        expanded = expanded,
//        onExpandedChange = { expanded = !expanded }
//    ) {
//        OutlinedTextField(
//            value = selectedText,
//            onValueChange = {},
//            readOnly = true,
//            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
//            modifier = Modifier
//                .menuAnchor()
//                .fillMaxWidth()
//        )
//        ExposedDropdownMenu(
//            expanded = expanded,
//            onDismissRequest = { expanded = false }
//        ) {
//            availableFranchises.forEach { franchise ->
//                DropdownMenuItem(
//                    text = { Text(franchise) },
//                    onClick = {
//                        onFranchiseChange(franchise)
//                        expanded = false
//                    }
//                )
//            }
//        }
//    }
//}

@Composable
private fun CardsGrid(
    cards: Cards,
    salesInfo: List<SalesInfoItem>,
    onSalesCardClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard("Total Sales", cards.totalSales, modifier = Modifier.weight(1f))
        }

        // Item Sales Card
        val totalRevenue = salesInfo.sumOf { it.revenue }
        val totalQty = salesInfo.sumOf { it.qty }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ItemSalesCard(
                totalRevenue = totalRevenue,
                totalQty = totalQty,
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onSalesCardClick)
            )
        }
    }
}


@Composable
private fun StatCard(title: String, rows: List<CardRow>, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                Icon(Icons.Outlined.Info, contentDescription = null, modifier = Modifier.size(14.dp))
            }
            rows.forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text(row.title, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Text(row.value, style = MaterialTheme.typography.titleMedium)
                    }
                    val color = if (row.deltaPct >= 0) Color(0xFF2E7D32) else Color(0xFFC62828)
                    Text(
                        (if (row.deltaPct >= 0) "↑" else "↓") + String.format("%.1f%%", kotlin.math.abs(row.deltaPct)),
                        color = color,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
            }
        }
    }
}

@Composable
private fun ItemSalesCard(
    totalRevenue: Double,
    totalQty: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Item Sales", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Revenue", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Text("₹${String.format("%.0f", totalRevenue)}", style = MaterialTheme.typography.titleMedium)
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text("Quantity", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Text("$totalQty", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
fun SalesInfoTable(salesInfo: List<SalesInfoItem>, textColor: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Item Name",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color(textColor),
                    modifier = Modifier.weight(2f)
                )
                Text(
                    text = "Qty",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color(textColor),
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                )
                Text(
                    text = "Revenue",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color(textColor),
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                )
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            salesInfo.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(textColor),
                        modifier = Modifier.weight(2f)
                    )
                    Text(
                        text = item.qty.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(textColor),
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.End
                    )
                    Text(
                        text = "₹${item.revenue}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(textColor),
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.End
                    )
                }
            }
        }
    }
}
