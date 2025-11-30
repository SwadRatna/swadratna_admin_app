package com.swadratna.swadratna_admin.ui.sales

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swadratna.swadratna_admin.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaleListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToNewSale: () -> Unit = {},
    onNavigateToVisualize: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Sale List",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "FAST v39.0 | 7906897228 | 1913",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
                    }
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = onNavigateToVisualize,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("VISUALIZE")
                }
                Button(
                    onClick = onNavigateToNewSale,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("NEW SALE")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
        ) {
            // Filter Section
            FilterSection()

            // Summary Section
            SummarySection()

            // Sales List
            SaleList()
        }
    }
}

@Composable
fun FilterSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // Dropdown (Mocked)
        OutlinedButton(
            onClick = { /* TODO */ },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Today", color = Color.Black)
                Icon(painter = painterResource(android.R.drawable.arrow_down_float), contentDescription = null, tint = Color.Black)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { /* TODO */ },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("24/11/25", color = Color.Black)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Black)
                }
            }
            OutlinedButton(
                onClick = { /* TODO */ },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("24/11/25", color = Color.Black)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(painter = painterResource(android.R.drawable.arrow_down_float), contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Black)
                }
            }
        }
    }
}

@Composable
fun SummarySection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Card(
            modifier = Modifier
                .weight(1f)
                .border(1.dp, Color.Green, RoundedCornerShape(8.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Text("Amount", style = MaterialTheme.typography.bodySmall)
                Text("₹ 11200", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
        Card(
            modifier = Modifier
                .weight(1f)
                .border(1.dp, Color.Blue, RoundedCornerShape(8.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Text("Count", style = MaterialTheme.typography.bodySmall)
                Text("64", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
        Box(
            modifier = Modifier
                .size(50.dp)
                .background(Color.LightGray, RoundedCornerShape(8.dp))
                .clickable { /* Clear Filter */ },
            contentAlignment = Alignment.Center
        ) {
             Icon(painter = painterResource(android.R.drawable.ic_menu_close_clear_cancel), contentDescription = "Clear", tint = Color.Gray)
        }
    }
}

data class SaleItemData(
    val title: String,
    val orderId: String,
    val date: String,
    val amount: String,
    val status: String,
    val editedBy: String,
    val isPaid: Boolean = true
)

@Composable
fun SaleList() {
    val mockData = listOf(
        SaleItemData("Parcal 1", "6912", "24/11/25", "₹ 140", "Paid", "Edited by Sundar kumar on 24/11/25 08:25 PM"),
        SaleItemData("Table No.4", "6911", "24/11/25", "₹ 40", "Paid", "Edited by Sundar kumar on 24/11/25 08:06 PM"),
        SaleItemData("Table No.2", "6910", "24/11/25", "₹ 80", "Paid", "Edited by Sundar kumar on 24/11/25 08:08 PM"),
        SaleItemData("Table No.7", "6909", "24/11/25", "₹ 30", "Paid", "Edited by Admin on 24/11/25 07:40 PM"),
        SaleItemData("Table No.1", "6911", "24/11/25", "₹ 140", "Paid", "Edited by Sundar kumar on 24/11/25 08:02 PM"),
        SaleItemData("Table No.5", "6910", "24/11/25", "₹ 60", "Paid", "Edited by Sundar kumar on 24/11/25 07:24 PM"),
        SaleItemData("Table No.9", "6909", "24/11/25", "₹ 20", "Paid", "Edited by Sundar kumar on 24/11/25 07:24 PM"),
    )

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(mockData) { item ->
            SaleItemRow(item)
        }
    }
}

@Composable
fun SaleItemRow(item: SaleItemData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color.LightGray)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${item.orderId} | ${item.date}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${item.amount} | ${item.status}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF4CAF50), // Green
                    fontWeight = FontWeight.Bold
                )
                 Icon(
                    painter = painterResource(android.R.drawable.checkbox_on_background), // Placeholder check
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = item.editedBy,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    fontSize = 10.sp,
                    modifier = Modifier.weight(1f)
                )
                OutlinedButton(
                    onClick = { /* TODO */ },
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Sale Return", fontSize = 10.sp)
                }
            }
        }
    }
}
