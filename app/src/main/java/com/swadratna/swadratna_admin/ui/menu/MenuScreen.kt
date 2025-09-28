package com.swadratna.swadratna_admin.ui.menu


import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.swadratna.swadratna_admin.data.model.MenuCategory

@Composable
fun MenuScreen(viewModel: MenuViewModel = hiltViewModel()) {

    val menuItems by viewModel.menuItems.collectAsState()

    // To handle status bar and bottom navigation properly
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // respect system bars
                .padding(16.dp)
        ) {

            Text("Menu Categories", fontSize = 20.sp, modifier = Modifier.padding(bottom = 8.dp))

            // Horizontally scrollable categories
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MenuCategory.values().forEach { category ->
                    Button(
                        onClick = { viewModel.selectCategory(category) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor =  Color(0xFFE0E0E0),
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(category.displayName)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("All Menu Items", fontSize = 20.sp)

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ){
                items(menuItems) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
                            )

                            Spacer(modifier = Modifier.height(8.dp).width(8.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.name, fontSize = 16.sp, fontWeight =FontWeight.Bold)
                                Text(item.description, fontSize = 14.sp, color = Color.Gray)
                                Text("Rs. ${item.price}", fontSize = 14.sp, color = Color(0xFF3366FF))
                                Text("Availability: ${item.availability}", fontSize = 12.sp, color = Color.Gray) }

                            Switch(
                                checked = item.isAvailable,
                                onCheckedChange = { viewModel.toggleAvailability(item) }
                            )
                        }
                    }
                }
            }
        }
    }
}

