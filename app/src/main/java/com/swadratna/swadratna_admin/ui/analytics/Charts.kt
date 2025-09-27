package com.swadratna.swadratna_admin.ui.analytics

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
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

@Composable
fun LineChartView(
    series: List<Series>,
    modifier: Modifier = Modifier
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
            }
        },
        update = { chart ->
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
    modifier: Modifier = Modifier
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
            }
        },
        update = { chart ->
            val dineInEntries = months.mapIndexed { i, m -> BarEntry(i.toFloat(), m.dineIn.toFloat()) }
            val deliveryEntries = months.mapIndexed { i, m -> BarEntry(i.toFloat(), m.delivery.toFloat()) }

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
            chart.xAxis.valueFormatter = IndexAxisValueFormatter(months.map { it.month })
            chart.xAxis.granularity = 1f
            chart.xAxis.axisMinimum = 0f
            chart.xAxis.axisMaximum = months.size.toFloat()
            chart.groupBars(0f, groupSpace, barSpace)
            chart.invalidate()
            chart.animateY(600)
        }
    )
}

@Composable
fun DonutChartView(
    categories: List<CategoryShare>,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            PieChart(ctx).apply {
                description.isEnabled = false
                isDrawHoleEnabled = true
                holeRadius = 65f
                setUsePercentValues(true)

                legend.apply {
                    isEnabled = true
                    verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                    horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
                    orientation = Legend.LegendOrientation.VERTICAL
                    setDrawInside(false)
                    xEntrySpace = 8f
                    yEntrySpace = 4f
                    textSize = 12f
                }

                setDrawEntryLabels(false)
            }
        },
        update = { chart ->
            val entries = categories.map { PieEntry(it.percent.toFloat(), it.name) }
            val ds = PieDataSet(entries, "").apply {
                sliceSpace = 2f
                colors = listOf(
                    Color(0xFF1565C0).toArgb(),
                    Color(0xFF2E7D32).toArgb(),
                    Color(0xFFF9A825).toArgb(),
                    Color(0xFF6D4C41).toArgb(),
                    Color(0xFF78909C).toArgb()
                )

                valueTextSize = 12f
                valueTextColor = Color.White.toArgb()
            }

            chart.data = PieData(ds)
            chart.animateY(600, Easing.EaseInOutQuad)
            chart.invalidate()
        }
    )
}


