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
import androidx.compose.ui.unit.dp
import com.swadratna.swadratna_admin.data.model.Campaign
import com.swadratna.swadratna_admin.data.model.CampaignStatus

@Composable
fun CampaignItem(
    campaign: Campaign,
    onViewDetails: (String) -> Unit,
    onEdit: (String) -> Unit = {},
    onDelete: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
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
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (campaign.status != CampaignStatus.SCHEDULED && campaign.status != CampaignStatus.DRAFT) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.AccountBox,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Performance: CTR: 5.2% (+0.8%)",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            TextButton(
                onClick = { onViewDetails(campaign.id) },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("View Details")
            }
        }
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