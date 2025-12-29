package com.swadratna.swadratna_admin.ui.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.swadratna.swadratna_admin.data.model.CategoryShare
import com.swadratna.swadratna_admin.data.model.MonthVolume
import com.swadratna.swadratna_admin.data.model.Series
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun LineChartView(
    series: List<Series>,
    modifier: Modifier = Modifier,
    textColor: Int = Color.Black.toArgb()
) {
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            LineChart(ctx).apply {
                description.isEnabled = false
                setTouchEnabled(true); isDragEnabled = true; setScaleEnabled(true); setPinchZoom(true)
                axisRight.isEnabled = false
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.setDrawGridLines(false)
                axisLeft.setDrawGridLines(true)
                legend.isEnabled = true
                
                // Set text colors
                xAxis.textColor = textColor
                axisLeft.textColor = textColor
                legend.textColor = textColor
            }
        },
        update = { chart ->
            // Update text colors on theme change
            chart.xAxis.textColor = textColor
            chart.axisLeft.textColor = textColor
            chart.legend.textColor = textColor

            val dataSets = series.mapIndexed { idx, s ->
                val entries = s.points.mapIndexed { i, p -> Entry(i.toFloat(), p.y.toFloat()) }
                LineDataSet(entries, s.name).apply {
                    lineWidth = 2.2f
                    circleRadius = 3.2f
                    setDrawValues(false)
                    mode = LineDataSet.Mode.CUBIC_BEZIER
                    color = when (idx) { 0 -> Color.Blue.toArgb(); 1 -> Color(0xFF2E7D32).toArgb(); else -> Color(0xFF6D4C41).toArgb() }
                    setCircleColor(color)
                }
            }
            val ld = LineData(dataSets)
            chart.data = ld
            chart.xAxis.valueFormatter = IndexAxisValueFormatter(series.firstOrNull()?.points?.map { it.xLabel } ?: emptyList())
            chart.animateX(600)
            chart.invalidate()
        }
    )
}

@Composable
fun GroupedBarChartView(
    months: List<MonthVolume>,
    modifier: Modifier = Modifier,
    textColor: Int = Color.Black.toArgb()
) {
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            BarChart(ctx).apply {
                description.isEnabled = false
                setTouchEnabled(true)
                axisRight.isEnabled = false
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.setDrawGridLines(false)
                axisLeft.setDrawGridLines(true)
                legend.isEnabled = true
                
                // Set text colors
                xAxis.textColor = textColor
                axisLeft.textColor = textColor
                legend.textColor = textColor
            }
        },
        update = { chart ->
            // Update text colors
            chart.xAxis.textColor = textColor
            chart.axisLeft.textColor = textColor
            chart.legend.textColor = textColor

            val now = LocalDate.now()
            val fmt = DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH)
            val lastSixLabels = (0..5).map { offset -> now.minusMonths((5 - offset).toLong()).format(fmt) }
            val filtered = months.filter { m -> lastSixLabels.contains(m.month) }
                .sortedBy { lastSixLabels.indexOf(it.month) }

            val dineInEntries = filtered.mapIndexed { i, m -> BarEntry(i.toFloat(), m.dineIn.toFloat()) }
            val deliveryEntries = filtered.mapIndexed { i, m -> BarEntry(i.toFloat(), m.delivery.toFloat()) }

            val ds1 = BarDataSet(dineInEntries, "Dine-In").apply {
                color = Color(0xFF1565C0).toArgb()
                setDrawValues(false)
            }
            val ds2 = BarDataSet(deliveryEntries, "Delivery").apply {
                color = Color(0xFF2E7D32).toArgb()
                setDrawValues(false)
            }
            val groupSpace = 0.2f
            val barSpace = 0.05f
            val barWidth = 0.35f
            val data = BarData(ds1, ds2)
            data.barWidth = barWidth

            chart.data = data
            chart.xAxis.valueFormatter = IndexAxisValueFormatter(lastSixLabels)
            chart.xAxis.granularity = 1f
            chart.xAxis.setCenterAxisLabels(true)
            val groupWidth = data.getGroupWidth(groupSpace, barSpace)
            chart.xAxis.axisMinimum = 0f
            chart.xAxis.axisMaximum = 0f + groupWidth * filtered.size
            chart.groupBars(0f, groupSpace, barSpace)
            chart.invalidate()
            chart.animateY(600)
        }
    )
}

@Composable
fun DonutChartView(
    categories: List<CategoryShare>,
    modifier: Modifier = Modifier,
    textColor: Int = Color.Black.toArgb()
) {
    val chartColors = listOf(
        Color(0xFF1565C0),
        Color(0xFF2E7D32),
        Color(0xFFF9A825),
        Color(0xFF6D4C41),
        Color(0xFF653D6E),
        Color(0xFF78909C)
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp),
            factory = { ctx ->
                PieChart(ctx).apply {
                    description.isEnabled = false
                    isDrawHoleEnabled = true
                    holeRadius = 65f
                    setUsePercentValues(true)
                    
                    // Set hole color to transparent or match background if needed, 
                    // but for now we just focus on text
                    setHoleColor(Color.Transparent.toArgb())

                    legend.isEnabled = false // Disable built-in legend

                    setDrawEntryLabels(false)
                }
            },
            update = { chart ->
                // Update text color
                chart.legend.textColor = textColor
                
                val entries = categories.map { PieEntry(it.percent.toFloat(), it.name) }
                val ds = PieDataSet(entries, "").apply {
                    sliceSpace = 2f
                    colors = chartColors.map { it.toArgb() }

                    valueTextSize = 12f
                    valueTextColor = Color.White.toArgb()
                }

                chart.data = PieData(ds)
                chart.animateY(600, Easing.EaseInOutQuad)
                chart.invalidate()
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Custom Legend
        val total = categories.sumOf { it.percent }
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEachIndexed { index, category ->
                val percentage = if (total > 0) (category.percent / total) * 100 else 0.0
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(chartColors.getOrElse(index % chartColors.size) { Color.Gray })
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(textColor),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = String.format("%.1f%%", percentage),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(textColor)
                    )
                }
            }
        }
    }
}


