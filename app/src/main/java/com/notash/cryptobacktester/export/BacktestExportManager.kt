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
    private val csvHeaders = listOf(
        "tradeNumber", "side", "entryPrice", "exitPrice", "timeframe", "entryTime", "exitTime",
        "positionSize", "leverage", "stopLoss", "takeProfit", "exitReason", "slTouched",
        "grossPnl", "netPnl", "pnlPercent", "fees", "funding", "status"
    )

    fun csv(report: BacktestReport): String = buildString {
        appendLine(csvHeaders.joinToString(","))
        report.trades.forEachIndexed { index, t ->
            appendLine(listOf(
                index + 1,
                t.side.name,
                t.entryPrice,
                t.exitPrice,
                csvCell(t.timeframe),
                t.entryTime,
                t.exitTime,
                t.quantity,
                t.leverage,
                t.stopLoss,
                t.takeProfit,
                csvCell(t.exitReason),
                t.slTouched,
                t.grossPnl,
                t.netPnl,
                t.pnlPercent,
                t.fees,
                t.funding,
                if (t.isWin) "WIN" else "LOSS"
            ).joinToString(","))
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
        root.put("timeframe", report.timeframe)
        root.put("leverage", report.leverage)
        root.put("equityCurve", JSONArray(report.equityCurve))

        val trades = JSONArray()
        report.trades.forEachIndexed { index, t ->
            trades.put(JSONObject().apply {
                put("tradeNumber", index + 1)
                put("side", t.side.name)
                put("entryPrice", t.entryPrice)
                put("exitPrice", t.exitPrice)
                put("timeframe", t.timeframe)
                put("entryTime", t.entryTime)
                put("exitTime", t.exitTime)
                put("positionSize", t.quantity)
                put("leverage", t.leverage)
                put("stopLoss", t.stopLoss)
                put("takeProfit", t.takeProfit)
                put("exitReason", t.exitReason)
                put("slTouched", t.slTouched)
                put("grossPnl", t.grossPnl)
                put("netPnl", t.netPnl)
                put("pnlPercent", t.pnlPercent)
                put("fees", t.fees)
                put("funding", t.funding)
                put("status", if (t.isWin) "WIN" else "LOSS")
            })
        }
        root.put("trades", trades)
        return root.toString(2)
    }

    private fun csvCell(value: String): String {
        if (value.none { it == ',' || it == '"' || it == '\n' || it == '\r' }) return value
        return "\"${value.replace("\"", "\"\"")}\""
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
