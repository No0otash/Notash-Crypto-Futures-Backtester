package com.notash.cryptobacktester.export

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.notash.cryptobacktester.core.BacktestReport
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.json.JSONArray
import org.json.JSONObject

enum class ExportStorageMode {
    MEDIA_STORE_DOWNLOADS,
    APP_EXTERNAL_FILES
}

fun exportStorageModeForSdk(sdk: Int): ExportStorageMode =
    if (sdk >= Build.VERSION_CODES.Q) ExportStorageMode.MEDIA_STORE_DOWNLOADS
    else ExportStorageMode.APP_EXTERNAL_FILES

object BacktestExportManager {
    fun csv(report: BacktestReport): String = buildString {
        appendLine("side,entryPrice,exitPrice,quantity,grossPnl,fees,funding,netPnl,entryTime,exitTime")
        report.trades.forEach { t ->
            appendLine(listOf(t.side, t.entryPrice, t.exitPrice, t.quantity, t.grossPnl, t.fees, t.funding, t.netPnl, t.entryTime, t.exitTime).joinToString(","))
        }
    }

    fun json(report: BacktestReport): String {
        val root = JSONObject()
        root.put("initialBalance", report.initialBalance)
        root.put("finalBalance", report.finalBalance)
        root.put("netPnl", report.netPnl)
        root.put("roiPercent", report.roiPercent)
        root.put("maxDrawdownPercent", report.maxDrawdownPercent)
        root.put("winRatePercent", report.winRatePercent)
        root.put("profitFactor", if (report.profitFactor.isFinite()) report.profitFactor else JSONObject.NULL)
        root.put("totalFees", report.totalFees)
        root.put("totalFunding", report.totalFunding)
        root.put("equityCurve", JSONArray(report.equityCurve))
        val trades = JSONArray()
        report.trades.forEach { t ->
            trades.put(JSONObject().apply {
                put("side", t.side.name); put("entryPrice", t.entryPrice); put("exitPrice", t.exitPrice)
                put("quantity", t.quantity); put("grossPnl", t.grossPnl); put("fees", t.fees)
                put("funding", t.funding); put("netPnl", t.netPnl); put("entryTime", t.entryTime); put("exitTime", t.exitTime)
            })
        }
        root.put("trades", trades)
        return root.toString(2)
    }

    fun aiReport(report: BacktestReport): String = buildString {
        appendLine("NOTASH AI TRADE ANALYSIS")
        appendLine("Final balance: %.4f".format(Locale.US, report.finalBalance))
        appendLine("Net PnL: %.4f".format(Locale.US, report.netPnl))
        appendLine("ROI: %.2f%%".format(Locale.US, report.roiPercent))
        appendLine("Max drawdown: %.2f%%".format(Locale.US, report.maxDrawdownPercent))
        appendLine("Win rate: %.2f%%".format(Locale.US, report.winRatePercent))
        appendLine("Profit factor: ${if (report.profitFactor.isFinite()) "%.3f".format(Locale.US, report.profitFactor) else "Infinity"}")
        appendLine("Total fees: %.4f".format(Locale.US, report.totalFees))
        appendLine("Total funding: %.4f".format(Locale.US, report.totalFunding))
        appendLine("Trades: ${report.trades.size}")
        appendLine()
        appendLine("AI review inputs: use net PnL, drawdown, win rate, fees, funding and trade-by-trade results to diagnose risk and strategy quality.")
    }

    fun save(context: Context, fileName: String, content: String, mime: String): Boolean =
        when (exportStorageModeForSdk(Build.VERSION.SDK_INT)) {
            ExportStorageMode.MEDIA_STORE_DOWNLOADS -> saveViaMediaStore(context, fileName, content, mime)
            ExportStorageMode.APP_EXTERNAL_FILES -> saveToAppExternalFiles(context, fileName, content)
        }

    private fun saveViaMediaStore(context: Context, fileName: String, content: String, mime: String): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return false
        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, mime)
            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/NotashCryptoBacktester")
        }
        val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values) ?: return false
        return try {
            context.contentResolver.openOutputStream(uri)?.use { it.write(content.toByteArray(Charsets.UTF_8)) }
                ?: return false
            true
        } catch (_: Exception) {
            context.contentResolver.delete(uri, null, null)
            false
        }
    }

    private fun saveToAppExternalFiles(context: Context, fileName: String, content: String): Boolean {
        return try {
            val downloads = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                ?: context.filesDir
            val directory = File(downloads, "NotashCryptoBacktester")
            if (!directory.exists() && !directory.mkdirs()) return false
            File(directory, fileName).writeText(content, Charsets.UTF_8)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun timestamp(): String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
}
