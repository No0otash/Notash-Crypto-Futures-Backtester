package com.notash.cryptobacktester.export

import com.notash.cryptobacktester.core.BacktestReport

/**
 * Backwards-compatible export facade. The export schema is owned by
 * BacktestExportManager so every caller receives the complete Trade Report fields.
 */
object BacktestExporter {
    fun toCsv(report: BacktestReport): String = BacktestExportManager.csv(report)

    fun toJson(report: BacktestReport): String = BacktestExportManager.json(report)
}
