package com.notash.cryptobacktester.ui

import com.notash.cryptobacktester.intelligence.MemeScanResult
import com.notash.cryptobacktester.intelligence.RadarPumpDumpSignal

sealed interface IntelligenceState {
    data object Loading : IntelligenceState
    data class Success(
        val pumpDump: List<RadarPumpDumpSignal>,
        val meme: List<MemeScanResult>,
        val scannedMarkets: Int,
        val unavailableMarkets: Int,
        val scannedAtMs: Long
    ) : IntelligenceState
    data class Empty(val reason: String) : IntelligenceState
    data class Error(val message: String) : IntelligenceState
    data class Offline(val cachedAtMs: Long?) : IntelligenceState
}
