package com.swadratna.swadratna_admin.ui.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(viewModel: MenuViewModel = hiltViewModel(), onBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val selected by viewModel.selectedCategory.collectAsState()

    LaunchedEffect(Unit) { viewModel.setUseMock(enabled = true) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Menu Categories") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) {  paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MenuCategory.values().forEach { category ->
                    val isSelected = category == selected
                    Button(
                        onClick = { viewModel.selectCategory(category) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) Color(0xFF3366FF) else Color(0xFFE0E0E0),
                            contentColor = if (isSelected) Color.White else Color.Black
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) { Text(category.displayName) }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("All Menu Items", fontSize = 20.sp)
            Spacer(modifier = Modifier.height(16.dp))

            when (val state = uiState) {
                is MenuUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                }
                is MenuUiState.Error -> {
                    Text("Failed to load menu: ${state.message}", color = Color.Red, modifier = Modifier.padding(16.dp))
                }
                is MenuUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.items) { item ->
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
                                        Text(item.name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                        Text(item.description, fontSize = 14.sp, color = Color.Gray)
                                        Text("Rs. ${item.price}", fontSize = 14.sp, color = Color(0xFF3366FF))
                                        Text("Availability: ${item.availability}", fontSize = 12.sp, color = Color.Gray)
                                    }
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
    }
}
