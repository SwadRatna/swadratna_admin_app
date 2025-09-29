package com.swadratna.swadratna_admin.ui.analytics

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.swadratna.swadratna_admin.data.model.*
import java.io.File

object AnalyticsCsvExporter {
    private const val AUTH_SUFFIX = ".fileprovider"

    fun exportToCsv(
        context: Context,
        franchise: String?,        // may be null if not in UiState
        analytics: Analytics,
        fileName: String = "analytics_export.csv"
    ): Uri {
        val rows = buildList {
            // Header info
            add(listOf("Franchise Filter", franchise ?: "All"))
            add(emptyList())

            // Cards
            add(listOf("Cards"))
            add(listOf("Section","Metric","Value","Delta%"))
            fun emitCard(section: String, items: List<CardRow>) {
                items.forEach { row ->
                    add(listOf(section, row.title, row.value, formatPct(row.deltaPct)))
                }
            }
            emitCard("Total Sales", analytics.cards.totalSales)
            emitCard("Total ROI", analytics.cards.totalRoi)
            emitCard("Customer Acquisition", analytics.cards.acquisition)
            emitCard("Average Order Value", analytics.cards.aov)
            add(emptyList())

            // Sales performance: one block per series
            analytics.salesPerformance.forEach { series ->
                add(listOf("Sales Performance - ${series.name}"))
                add(listOf("Label","Value"))
                series.points.forEach { p ->
                    add(listOf(p.xLabel, p.y.toString()))
                }
                add(emptyList())
            }

            // Monthly order volume
            add(listOf("Monthly Order Volume"))
            add(listOf("Month","Dine-in","Delivery"))
            analytics.monthlyOrderVolume.forEach { m ->
                add(listOf(m.month, m.dineIn.toString(), m.delivery.toString()))
            }
            add(emptyList())

            // Top product categories
            add(listOf("Top Selling Product Categories"))
            add(listOf("Category","Percent"))
            analytics.topProductCategories.forEach { c ->
                add(listOf(c.name, formatPct(c.percent)))
            }
        }

        val csv = buildString {
            rows.forEach { line ->
                append(line.joinToString(",") { escapeCsv(it) })
                append('\n')
            }
        }

        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        val outFile = File(dir, fileName)
        outFile.writeText(csv)

        return FileProvider.getUriForFile(
            context,
            context.packageName + AUTH_SUFFIX,
            outFile
        )
    }

    private fun formatPct(value: Double): String =
        String.format("%.2f", value)

    private fun escapeCsv(value: String): String {
        // Quote if comma, quote, or newline is present; double any internal quotes.
        val needsQuote = value.indexOfAny(charArrayOf(',', '"', '\n', '\r')) >= 0
        if (!needsQuote) return value
        val doubled = value.replace("\"", "\"\"")
        return "\"$doubled\""
    }
}
