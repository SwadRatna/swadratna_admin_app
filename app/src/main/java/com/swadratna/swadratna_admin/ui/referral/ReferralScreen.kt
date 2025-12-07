package com.swadratna.swadratna_admin.ui.referral

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.swadratna.swadratna_admin.data.model.Withdrawal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReferralScreen(
    viewModel: ReferralViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedStatus by remember { mutableStateOf<String?>(null) }
    var showActionDialog by remember { mutableStateOf<Pair<Withdrawal, String>?>(null) } // Withdrawal to Action (Approve, Reject, Process)

    LaunchedEffect(selectedStatus) {
        viewModel.fetchWithdrawals(selectedStatus)
    }
    
    // Handle action messages and errors
    LaunchedEffect(uiState.actionMessage) {
        if (uiState.actionMessage != null) {
            viewModel.clearActionMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Referral Withdrawals") },
                actions = {
                    IconButton(onClick = { viewModel.fetchWithdrawals(selectedStatus) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(listOf(
                    null to "All",
                    "pending" to "Pending",
                    "approved" to "Approved",
                    "rejected" to "Rejected",
                    "processed" to "Processed"
                )) { (status, label) ->
                    FilterChip(
                        selected = selectedStatus == status,
                        onClick = { selectedStatus = status },
                        label = { Text(label) }
                    )
                }
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.error != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = uiState.error ?: "Unknown error", color = Color.Red)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.withdrawals) { withdrawal ->
                        WithdrawalItem(
                            withdrawal = withdrawal,
                            onApprove = { showActionDialog = it to "approve" },
                            onReject = { showActionDialog = it to "reject" },
                            onProcess = { showActionDialog = it to "process" }
                        )
                    }
                }
            }
        }
    }

    showActionDialog?.let { (withdrawal, action) ->
        ActionDialog(
            withdrawal = withdrawal,
            action = action,
            onDismiss = { showActionDialog = null },
            onConfirm = { remarks, reason, ref ->
                when (action) {
                    "approve" -> viewModel.approveWithdrawal(withdrawal.id, ref, remarks)
                    "reject" -> viewModel.rejectWithdrawal(withdrawal.id, reason)
                    "process" -> viewModel.processWithdrawal(withdrawal.id, ref, remarks)
                }
                showActionDialog = null
            }
        )
    }
}

@Composable
fun WithdrawalItem(
    withdrawal: Withdrawal,
    onApprove: (Withdrawal) -> Unit,
    onReject: (Withdrawal) -> Unit,
    onProcess: (Withdrawal) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = withdrawal.userName ?: "Unknown User",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "₹${withdrawal.cashAmount}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(text = "Phone: ${withdrawal.userPhone ?: "N/A"}")
            Text(text = "Payment Method: ${withdrawal.paymentMethod}")
            Text(text = "Points: ${withdrawal.points}")
            Text(text = "Date: ${withdrawal.createdAt}")
            Text(text = "Status: ${withdrawal.status}", color = getStatusColor(withdrawal.status))
            
            if (withdrawal.status == "pending") {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = { onReject(withdrawal) }, modifier = Modifier.padding(end = 8.dp)) {
                        Text("Reject")
                    }
                    Button(onClick = { onApprove(withdrawal) }) {
                        Text("Approve")
                    }
                }
            } else if (withdrawal.status == "approved") {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(onClick = { onProcess(withdrawal) }) {
                        Text("Mark Processed")
                    }
                }
            }
        }
    }
}

@Composable
fun ActionDialog(
    withdrawal: Withdrawal,
    action: String,
    onDismiss: () -> Unit,
    onConfirm: (remarks: String?, reason: String?, ref: String?) -> Unit
) {
    var remarks by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    var ref by remember { mutableStateOf("") }
    var showValidationError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = action.replaceFirstChar { it.uppercase() } + " Withdrawal") },
        text = {
            Column {
                if (action == "reject") {
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Reason") },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    OutlinedTextField(
                        value = ref,
                        onValueChange = { 
                            ref = it
                            if (showValidationError) showValidationError = false
                        },
                        label = { Text("Transaction Reference") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = showValidationError
                    )
                    if (showValidationError) {
                        Text(
                            text = "Enter n/a in transaction reference",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = remarks,
                        onValueChange = { remarks = it },
                        label = { Text("Remarks") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = { 
                if (action == "reject") {
                    onConfirm(remarks, reason, ref)
                } else {
                    if (ref.isBlank()) {
                        showValidationError = true
                    } else {
                        onConfirm(remarks, reason, ref)
                    }
                }
            }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

fun getStatusColor(status: String): Color {
    return when (status) {
        "pending" -> Color(0xFFFFA500) // Orange
        "approved" -> Color(0xFF4CAF50) // Green
        "rejected" -> Color.Red
        "processed" -> Color.Blue
        else -> Color.Gray
    }
}
