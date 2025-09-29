//package com.swadratna.swadratna_admin.ui.analytics
//
//import android.graphics.Bitmap
//import android.graphics.Canvas
//import com.github.mikephil.charting.charts.BarChart
//import com.github.mikephil.charting.charts.LineChart
//import com.github.mikephil.charting.charts.PieChart
//import android.content.Context
//import com.github.mikephil.charting.data.*
//import com.github.mikephil.charting.components.XAxis
//import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
//import com.swadratna.swadratna_admin.data.model.*
//
//fun LineChart.toBitmap(): Bitmap {
//    measure(
//        android.view.View.MeasureSpec.makeMeasureSpec(width, android.view.View.MeasureSpec.EXACTLY),
//        android.view.View.MeasureSpec.makeMeasureSpec(height, android.view.View.MeasureSpec.EXACTLY)
//    )
//    layout(0, 0, measuredWidth, measuredHeight)
//    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
//    val canvas = Canvas(bitmap)
//    draw(canvas)
//    return bitmap
//}
//
//fun BarChart.toBitmap(): Bitmap {
//    measure(
//        android.view.View.MeasureSpec.makeMeasureSpec(width, android.view.View.MeasureSpec.EXACTLY),
//        android.view.View.MeasureSpec.makeMeasureSpec(height, android.view.View.MeasureSpec.EXACTLY)
//    )
//    layout(0, 0, measuredWidth, measuredHeight)
//    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
//    val canvas = Canvas(bitmap)
//    draw(canvas)
//    return bitmap
//}
//
//fun PieChart.toBitmap(): Bitmap {
//    measure(
//        android.view.View.MeasureSpec.makeMeasureSpec(width, android.view.View.MeasureSpec.EXACTLY),
//        android.view.View.MeasureSpec.makeMeasureSpec(height, android.view.View.MeasureSpec.EXACTLY)
//    )
//    layout(0, 0, measuredWidth, measuredHeight)
//    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
//    val canvas = Canvas(bitmap)
//    draw(canvas)
//    return bitmap
//}
//
//
//fun List<Series>.toLineChartBitmap(context: Context, width: Int = 600, height: Int = 300): Bitmap {
//    val chart = LineChart(context)
//    chart.apply {
//        description.isEnabled = false
//        axisRight.isEnabled = false
//        xAxis.position = XAxis.XAxisPosition.BOTTOM
//        xAxis.setDrawGridLines(false)
//        axisLeft.setDrawGridLines(true)
//        legend.isEnabled = true
//
//        val dataSets = this@toLineChartBitmap.mapIndexed { idx, s ->
//            val entries = s.points.mapIndexed { i, p -> Entry(i.toFloat(), p.y.toFloat()) }
//            LineDataSet(entries, s.name).apply {
//                lineWidth = 2.2f
//                circleRadius = 3.2f
//                setDrawValues(false)
//                mode = LineDataSet.Mode.CUBIC_BEZIER
//            }
//        }
//
//        data = LineData(dataSets)
//        xAxis.valueFormatter = IndexAxisValueFormatter(this@toLineChartBitmap.firstOrNull()?.points?.map { it.xLabel } ?: emptyList())
//        layout(0, 0, width, height)
//    }
//    return chart.toBitmap()
//}
//
//fun List<MonthVolume>.toBarChartBitmap(context: Context, width: Int = 600, height: Int = 300): Bitmap {
//    val chart = BarChart(context)
//    chart.apply {
//        description.isEnabled = false
//        axisRight.isEnabled = false
//        xAxis.position = XAxis.XAxisPosition.BOTTOM
//        xAxis.setDrawGridLines(false)
//        axisLeft.setDrawGridLines(true)
//        legend.isEnabled = true
//
//        val dineInEntries = this@toBarChartBitmap.mapIndexed { i, m -> BarEntry(i.toFloat(), m.dineIn.toFloat()) }
//        val deliveryEntries = this@toBarChartBitmap.mapIndexed { i, m -> BarEntry(i.toFloat(), m.delivery.toFloat()) }
//
//        val ds1 = BarDataSet(dineInEntries, "Dine-In").apply { setDrawValues(false) }
//        val ds2 = BarDataSet(deliveryEntries, "Delivery").apply { setDrawValues(false) }
//
//        val data = BarData(ds1, ds2).apply { barWidth = 0.35f }
//        this.data = data
//
//        xAxis.valueFormatter = IndexAxisValueFormatter(this@toBarChartBitmap.map { it.month })
//        xAxis.granularity = 1f
//        xAxis.axisMinimum = 0f
//        xAxis.axisMaximum = this@toBarChartBitmap.size.toFloat()
//        groupBars(0f, 0.2f, 0.05f)
//
//        layout(0, 0, width, height)
//    }
//    return chart.toBitmap()
//}
//
//fun List<CategoryShare>.toDonutChartBitmap(context: Context, width: Int = 400, height: Int = 400): Bitmap {
//    val chart = PieChart(context)
//    chart.apply {
//        description.isEnabled = false
//        isDrawHoleEnabled = true
//        holeRadius = 65f
//        setUsePercentValues(true)
//        legend.isEnabled = true
//        setDrawEntryLabels(false)
//
//        val entries = this@toDonutChartBitmap.map { PieEntry(it.percent.toFloat(), it.name) }
//        val ds = PieDataSet(entries, "").apply {
//            sliceSpace = 2f
//            setDrawValues(false)
//        }
//        data = PieData(ds)
//
//        layout(0, 0, width, height)
//    }
//    return chart.toBitmap()
//}