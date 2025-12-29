package com.swadratna.swadratna_admin.ui.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.swadratna.swadratna_admin.data.model.SalesInfoItem

@Composable
fun SalesDetailsDialog(
    salesInfo: List<SalesInfoItem>,
    onDismiss: () -> Unit,
    textColor: Int
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Item Sales Details") },
        text = {
            if (salesInfo.isEmpty()) {
                Text(
                    "No sell happened today",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    SalesInfoTable(salesInfo, textColor)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
