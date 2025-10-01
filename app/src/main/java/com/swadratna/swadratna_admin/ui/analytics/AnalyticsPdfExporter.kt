//package com.swadratna.swadratna_admin.ui.analytics
//
//import android.content.ContentValues
//import android.content.Context
//import android.graphics.Canvas
//import android.graphics.Paint
//import android.graphics.pdf.PdfDocument
//import android.os.Build
//import android.os.Environment
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.toArgb
//import java.io.File
//import java.io.FileOutputStream
//import com.swadratna.swadratna_admin.data.model.Analytics
//import com.swadratna.swadratna_admin.data.model.Cards
//import java.io.ByteArrayOutputStream
//
//
//object AnalyticsPdfExporter {
//
//    fun exportAnalyticsPdf(context: Context, analytics: Analytics) {
//        val pdfDocument = PdfDocument()
//        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
//        val page = pdfDocument.startPage(pageInfo)
//        val canvas = page.canvas
//        val paint = Paint().apply {
//            color = Color.Black.toArgb()
//            textSize = 14f
//        }
//
//        var yPos = 30f
//        val margin = 20f
//
//        // 1️⃣ Header
//        paint.textSize = 18f
//        canvas.drawText("Analytics Report", margin, yPos, paint)
//        paint.textSize = 14f
//        yPos += 30f
//
//        // 2️⃣ Cards
//        drawCards(analytics.cards, canvas, paint, margin) { yPos = it }
//
//        yPos += 20f
//
//        // 3️⃣ Charts
//        analytics.salesPerformance.toLineChartBitmap(context)?.let { bitmap ->
//            canvas.drawBitmap(bitmap, margin, yPos, paint)
//            yPos += bitmap.height + 20f
//        }
//
//        analytics.monthlyOrderVolume.toBarChartBitmap(context)?.let { bitmap ->
//            canvas.drawBitmap(bitmap, margin, yPos, paint)
//            yPos += bitmap.height + 20f
//        }
//
//        analytics.topProductCategories.toDonutChartBitmap(context)?.let { bitmap ->
//            canvas.drawBitmap(bitmap, margin, yPos, paint)
//            yPos += bitmap.height + 20f
//        }
//
//        pdfDocument.finishPage(page)
//
//        val pdfBytes = ByteArrayOutputStream().apply { pdfDocument.writeTo(this) }.toByteArray()
//        pdfDocument.close()
//
//        savePdfToDownloads(context, "AnalyticsReport.pdf", pdfBytes)
//    }
//
//    private fun drawCards(cards: Cards, canvas: Canvas, paint: Paint, margin: Float, updateY: (Float) -> Unit) {
//        var yPos = margin
//        val rowSpacing = 18f
//        val sectionSpacing = 12f
//
//        listOf(
//            "Total Sales" to cards.totalSales,
//            "Total ROI" to cards.totalRoi,
//            "Customer Acquisition" to cards.acquisition,
//            "Average Order Value" to cards.aov
//        ).forEach { (title, rows) ->
//            paint.isFakeBoldText = true
//            canvas.drawText(title, margin, yPos, paint)
//            paint.isFakeBoldText = false
//            yPos += rowSpacing
//            rows.forEach { row ->
//                canvas.drawText("  ${row.title}: ${row.value} (${row.deltaPct}%)", margin + 10f, yPos, paint)
//                yPos += rowSpacing
//            }
//            yPos += sectionSpacing
//        }
//
//        updateY(yPos)
//    }
//
//    private fun savePdfToDownloads(context: Context, fileName: String, pdfBytes: ByteArray) {
//        try {
//            val fos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                val values = ContentValues().apply {
//                    put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName)
//                    put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
//                    put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
//                }
//                val uri = context.contentResolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
//                context.contentResolver.openOutputStream(uri!!)
//            } else {
//                val downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
//                if (!downloads.exists()) downloads.mkdirs()
//                val file = File(downloads, fileName)
//                FileOutputStream(file)
//            }
//
//            fos?.write(pdfBytes)
//            fos?.close()
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }
//}
