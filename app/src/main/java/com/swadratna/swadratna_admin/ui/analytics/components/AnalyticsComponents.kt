package com.swadratna.swadratna_admin.ui.analytics.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swadratna.swadratna_admin.data.model.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun MetricCard(
    title: String,
    value: String,
    percentChange: Float,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                
                val changeColor = if (percentChange >= 0) Color(0xFF34A853) else Color(0xFFEA4335)
                val changePrefix = if (percentChange >= 0) "+" else ""
                
                Text(
                    text = "$changePrefix${percentChange}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = changeColor
                )
            }
        }
    }
}

@Composable
fun FranchiseMetricItem(
    franchiseName: String,
    value: String,
    percentChange: Float,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = franchiseName,
            style = MaterialTheme.typography.bodyMedium
        )
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            
            val changeColor = if (percentChange >= 0) Color(0xFF34A853) else Color(0xFFEA4335)
            val changePrefix = if (percentChange >= 0) "+" else ""
            
            Text(
                text = "$changePrefix${percentChange}%",
                style = MaterialTheme.typography.bodySmall,
                color = changeColor
            )
        }
    }
    
    Divider(modifier = Modifier.padding(vertical = 4.dp))
}

@Composable
fun LineChart(
    data: List<PerformancePoint>,
    modifier: Modifier = Modifier,
    lineColor: Color = Color(0xFF4285F4)
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp)
            .padding(16.dp)
    ) {
        val width = size.width
        val height = size.height
        val maxValue = data.maxOfOrNull { it.value } ?: 1f
        val minValue = data.minOfOrNull { it.value } ?: 0f
        val range = (maxValue - minValue).coerceAtLeast(0.1f)
        
        val path = Path()
        val points = data.mapIndexed { index, point ->
            val x = width * index / (data.size - 1)
            val y = height - (height * (point.value - minValue) / range)
            Offset(x, y)
        }
        
        if (points.isNotEmpty()) {
            path.moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
                path.lineTo(points[i].x, points[i].y)
            }
            
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 3f, cap = StrokeCap.Round)
            )
            
            points.forEach { point ->
                drawCircle(
                    color = lineColor,
                    radius = 4f,
                    center = point
                )
            }
        }
    }
}

@Composable
fun BarChart(
    data: List<MonthlyVolumeData>,
    modifier: Modifier = Modifier
) {
    val maxValue = data.maxOfOrNull { maxOf(it.dineIn, it.takeaway) }?.times(1.2f) ?: 1f
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEach { monthData ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                // Dine-in bar
                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .height((monthData.dineIn / maxValue * 150).dp)
                        .background(Color(0xFF4285F4), RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                )
                
                // Takeaway bar
                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .height((monthData.takeaway / maxValue * 150).dp)
                        .background(Color(0xFF34A853), RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        .padding(top = 4.dp)
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = monthData.month,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 10.sp
                )
            }
        }
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(end = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(Color(0xFF4285F4), CircleShape)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Dine-in",
                style = MaterialTheme.typography.bodySmall
            )
        }
        
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(Color(0xFF34A853), CircleShape)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Takeaway",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun PieChart(
    categories: List<ProductCategory>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(200.dp)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val total = categories.sumOf { it.percentage.toDouble() }.toFloat()
            var startAngle = -90f
            
            categories.forEach { category ->
                val sweepAngle = 360f * (category.percentage / total)
                val color = Color(android.graphics.Color.parseColor(category.color))
                
                drawArc(
                    color = color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true
                )
                
                startAngle += sweepAngle
            }
            
            // Draw a white circle in the middle for donut chart effect
            drawCircle(
                color = Color.White,
                radius = size.minDimension * 0.25f
            )
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        categories.forEach { category ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(Color(android.graphics.Color.parseColor(category.color)), CircleShape)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                
                Text(
                    text = "${category.percentage.toInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun TimeFrameSelector(
    timeFrame: String,
    onTimeFrameSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Timeframe:",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        TextButton(
            onClick = { onTimeFrameSelected(timeFrame) },
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = timeFrame,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}