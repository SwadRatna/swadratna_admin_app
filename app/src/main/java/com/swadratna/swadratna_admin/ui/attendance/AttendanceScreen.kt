package com.swadratna.swadratna_admin.ui.attendance

import android.app.DatePickerDialog
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.swadratna.swadratna_admin.data.model.AttendanceDay
import com.swadratna.swadratna_admin.data.model.AttendanceStaff
import com.swadratna.swadratna_admin.data.model.AttendanceSummary
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    storeId: String,
    onNavigateBack: () -> Unit,
    viewModel: AttendanceViewModel = hiltViewModel()
) {
    LaunchedEffect(storeId) {
        viewModel.fetchAttendance(storeId)
    }
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Attendance") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.fetchAttendance(storeId) }) {
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
            // Date Selection Row
            DateSelectionRow(
                fromDate = uiState.fromDate,
                toDate = uiState.toDate,
                onFromDateClick = {
                    showDatePicker(context, uiState.fromDate) { date ->
                        viewModel.updateDateRange(date, uiState.toDate)
                        viewModel.fetchAttendance(storeId)
                    }
                },
                onToDateClick = {
                    showDatePicker(context, uiState.toDate) { date ->
                        viewModel.updateDateRange(uiState.fromDate, date)
                        viewModel.fetchAttendance(storeId)
                    }
                }
            )

            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (uiState.error != null) {
                    Text(
                        text = uiState.error ?: "Unknown error",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    if (uiState.fromDate == uiState.toDate) {
                        // Daily View
                        val attendanceDay = uiState.attendanceResponse?.attendance?.firstOrNull()
                        if (attendanceDay != null) {
                            AttendanceContent(attendanceDay)
                        } else {
                            Text(
                                text = "No attendance data for this date",
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    } else {
                        // Range View (Aggregated)
                        AggregatedAttendanceContent(
                            aggregatedStaff = uiState.aggregatedStaff,
                            fromDate = uiState.fromDate,
                            toDate = uiState.toDate
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DateSelectionRow(
    fromDate: String,
    toDate: String,
    onFromDateClick: () -> Unit,
    onToDateClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(
            onClick = onFromDateClick,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = fromDate,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        OutlinedButton(
            onClick = onToDateClick,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = toDate,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun AggregatedAttendanceContent(
    aggregatedStaff: List<StaffAttendanceAggregate>,
    fromDate: String,
    toDate: String
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Summary ($fromDate to $toDate)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (aggregatedStaff.isEmpty()) {
            item {
                Text("No data found for this range")
            }
        } else {
            items(aggregatedStaff) { staff ->
                AggregatedStaffItem(staff)
            }
        }
    }
}

@Composable
fun AggregatedStaffItem(staff: StaffAttendanceAggregate) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = staff.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatColumn("Present", staff.presentCount.toString(), Color(0xFF4CAF50))
                StatColumn("Absent", staff.absentCount.toString(), Color(0xFFF44336))
                StatColumn("Half Day", staff.halfDayCount.toString(), Color(0xFFEF6C00))
                StatColumn("Leave", staff.leaveCount.toString(), Color(0xFF1565C0))
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total Hours: ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = String.format("%.1f", staff.totalHours),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Salary: ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = "₹${String.format("%.0f", staff.calculatedSalary)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun StatColumn(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun AttendanceContent(attendanceDay: AttendanceDay) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            AttendanceSummaryCard(attendanceDay.summary)
        }
        
        item {
            Text(
                text = "Staff Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(attendanceDay.staff) { staff ->
            StaffAttendanceItem(staff)
        }
    }
}

@Composable
fun AttendanceSummaryCard(summary: AttendanceSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Today's Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryItem(label = "Total", value = summary.total.toString())
                SummaryItem(label = "Present", value = summary.present.toString(), color = Color(0xFF4CAF50))
                SummaryItem(label = "Absent", value = summary.absent.toString(), color = Color(0xFFF44336))
            }
        }
    }
}

@Composable
fun SummaryItem(label: String, value: String, color: Color = MaterialTheme.colorScheme.onSurface) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun StaffAttendanceItem(staff: AttendanceStaff) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                    text = staff.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                StatusChip(status = staff.status)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Check In",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = staff.checkIn ?: "-",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                Column {
                    Text(
                        text = "Check Out",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = staff.checkOut ?: "-",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                Column {
                    Text(
                        text = "Hours",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = String.format("%.1f", staff.totalHours),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun StatusChip(status: String) {
    val (backgroundColor, textColor) = when (status.lowercase()) {
        "present" -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        "absent" -> Color(0xFFFFEBEE) to Color(0xFFC62828)
        "half_day" -> Color(0xFFFFF3E0) to Color(0xFFEF6C00)
        "on_leave" -> Color(0xFFE3F2FD) to Color(0xFF1565C0)
        else -> Color(0xFFF5F5F5) to Color(0xFF616161)
    }

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(16.dp),
    ) {
        Text(
            text = status.replace("_", " ").capitalize(),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Medium
        )
    }
}

fun String.capitalize(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}

fun showDatePicker(context: Context, currentDate: String, onDateSelected: (String) -> Unit) {
    val parts = currentDate.split("-")
    val year = parts[0].toInt()
    val month = parts[1].toInt() - 1 // DatePickerDialog uses 0-indexed months
    val day = parts[2].toInt()

    DatePickerDialog(
        context,
        { _, selectedYear, selectedMonth, selectedDay ->
            val formattedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
            onDateSelected(formattedDate)
        },
        year,
        month,
        day
    ).show()
}
