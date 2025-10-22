package com.swadratna.swadratna_admin.ui.campaign.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.AccountBox
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.swadratna.swadratna_admin.data.model.Campaign
import com.swadratna.swadratna_admin.data.model.CampaignStatus
import androidx.compose.foundation.clickable
import java.time.LocalDate

@Composable
fun CampaignItem(
    campaign: Campaign,
    onViewDetails: (String) -> Unit = {},
    onEdit: (String) -> Unit = {},
    onDelete: (String) -> Unit = {},
    onChangeStatus: (String, CampaignStatus) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    var showStatusDialog by remember { mutableStateOf(false) }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onEdit(campaign.id) },
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // removed duplicate showStatusDialog
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = campaign.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Box {
                    var expanded by remember { mutableStateOf(false) }
                    
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More options")
                    }
                    
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = { 
                                expanded = false
                                onEdit(campaign.id)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = { 
                                expanded = false
                                onDelete(campaign.id)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Change Status") },
                            onClick = { 
                                expanded = false
                                // open status selection
                                showStatusDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Duplicate") },
                            onClick = { expanded = false }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            CampaignStatusChip(status = campaign.status)
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = campaign.getFormattedDateRange(),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Offer: ${campaign.description}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Show "Delete" text button only when the campaign has reached its end date or status is Completed
            val now = LocalDate.now()
            val showDeleteTextButton = (campaign.status == CampaignStatus.COMPLETED) || !now.isBefore(campaign.endDate)
            if (showDeleteTextButton) {
                TextButton(
                    onClick = { onDelete(campaign.id) },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = "Delete",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        textDecoration = TextDecoration.Underline
                    )
                }
            }
        }
    }
    if (showStatusDialog) {
        AlertDialog(
            onDismissRequest = { showStatusDialog = false },
            confirmButton = {
                // handled via selection list
            },
            dismissButton = {
                TextButton(onClick = { showStatusDialog = false }) { Text("Close") }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Change Status", style = MaterialTheme.typography.titleMedium)
                    Divider()
                    StatusOptionItem("Active", CampaignStatus.ACTIVE) {
                        onChangeStatus(campaign.id, CampaignStatus.ACTIVE)
                        showStatusDialog = false
                    }
                    StatusOptionItem("Scheduled", CampaignStatus.SCHEDULED) {
                        onChangeStatus(campaign.id, CampaignStatus.SCHEDULED)
                        showStatusDialog = false
                    }
                    StatusOptionItem("Completed", CampaignStatus.COMPLETED) {
                        onChangeStatus(campaign.id, CampaignStatus.COMPLETED)
                        showStatusDialog = false
                    }
                    StatusOptionItem("Draft", CampaignStatus.DRAFT) {
                        onChangeStatus(campaign.id, CampaignStatus.DRAFT)
                        showStatusDialog = false
                    }
                }
            }
        )
    }
}

@Composable
private fun StatusOptionItem(label: String, status: CampaignStatus, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AssistChip(onClick = onClick, label = { Text(label) })
    }
}
@Composable
fun CampaignStatusChip(status: CampaignStatus) {
    val (backgroundColor, contentColor) = when (status) {
        CampaignStatus.ACTIVE -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        CampaignStatus.SCHEDULED -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        CampaignStatus.COMPLETED -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
        CampaignStatus.DRAFT -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
    }
    
    val statusText = when (status) {
        CampaignStatus.ACTIVE -> "Active"
        CampaignStatus.SCHEDULED -> "Scheduled"
        CampaignStatus.COMPLETED -> "Completed"
        CampaignStatus.DRAFT -> "Draft"
    }
    
    SuggestionChip(
        onClick = { },
        label = { Text(statusText) },
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = backgroundColor,
            labelColor = contentColor
        )
    )
}