package com.pingoo.app.domain.history

import android.content.Intent
import com.pingoo.app.domain.model.JourneyHistoryItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object HistoryExportHelper {

    fun sanitizeCsvField(value: String): String {
        var clean = value.replace("\"", "\"\"")
        // Prevent CSV / Spreadsheet Formula Injection (CWE-1236)
        if (clean.isNotEmpty() && (clean.startsWith("=") || clean.startsWith("+") || clean.startsWith("-") || clean.startsWith("@") || clean.startsWith("\t") || clean.startsWith("\r"))) {
            clean = "'$clean"
        }
        return clean
    }

    fun generateCsv(historyItems: List<JourneyHistoryItem>): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        val sb = StringBuilder()
        sb.append("ID,Title,TotalStops,ReachedStops,Completed,DurationSeconds,DateTime\n")

        for (item in historyItems) {
            val safeTitle = sanitizeCsvField(item.title)
            val dateStr = dateFormat.format(Date(item.timestampMillis))
            sb.append("${item.id},\"$safeTitle\",${item.totalStops},${item.reachedStops},${item.isCompleted},${item.durationSeconds},\"$dateStr\"\n")
        }
        return sb.toString()
    }

    fun createShareIntent(csvContent: String, subject: String = "Pingoo Journey History Export"): Intent {
        return Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, csvContent)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
}
