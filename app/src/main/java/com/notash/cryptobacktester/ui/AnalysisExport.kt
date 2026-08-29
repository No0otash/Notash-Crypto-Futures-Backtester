package com.notash.cryptobacktester.ui

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.notash.cryptobacktester.ai.TradeAiAnalyzer
import com.notash.cryptobacktester.ai.TradeAnalysisExporter
import com.notash.cryptobacktester.core.BacktestReport
import java.io.File

object AnalysisExport {
    fun shareCsv(context: Context, report: BacktestReport, analysis: TradeAiAnalyzer.Analysis) = share(context, "trade_analysis.csv", "text/csv", TradeAnalysisExporter.csv(report, analysis))
    fun shareJson(context: Context, report: BacktestReport, analysis: TradeAiAnalyzer.Analysis) = share(context, "trade_analysis.json", "application/json", TradeAnalysisExporter.json(report, analysis))
    private fun share(context: Context, name: String, type: String, body: String) {
        val file = File(context.cacheDir, name).apply { writeText(body, Charsets.UTF_8) }
        val uri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
        context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply { this.type = type; putExtra(Intent.EXTRA_STREAM, uri); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }, "Export Analysis"))
    }
}
