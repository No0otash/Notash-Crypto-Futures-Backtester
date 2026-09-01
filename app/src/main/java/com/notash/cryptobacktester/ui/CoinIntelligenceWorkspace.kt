package com.notash.cryptobacktester.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notash.cryptobacktester.data.CoinExRepository
import com.notash.cryptobacktester.intelligence.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val IntelBg = Color(0xFF070A10)
private val IntelPanel = Color(0xFF0F1722)
private val IntelPanel2 = Color(0xFF151F2C)
private val IntelMint = Color(0xFF12C8B5)
private val IntelGreen = Color(0xFF2BD69A)
private val IntelRed = Color(0xFFFF6074)
private val IntelGold = Color(0xFFFFC857)
private val IntelMuted = Color(0xFF8995A8)

data class CoinIntelligenceRuntimeState(val loading: Boolean = true, val error: String? = null, val ui: CoinIntelligenceUiModel? = null, val pumpDump: PumpDumpSignal? = null, val providerHealth: List<ProviderHealth> = emptyList(), val deepGaps: List<String> = emptyList())

/** Live Coin Intelligence. Existing market/Pump-Dump behaviour is preserved; deep providers enrich it when chain metadata is available. */
@Composable
fun CoinIntelligenceWorkspace(
    persian: Boolean,
    market: String,
    repository: CoinExRepository = remember { CoinExRepository() },
    chain: String? = null,
    contractOrMint: String? = null
) {
    val scope = rememberCoroutineScope()
    var state by remember(market, chain, contractOrMint) { mutableStateOf(CoinIntelligenceRuntimeState()) }

    fun refresh() {
        scope.launch {
            state = state.copy(loading = true, error = null)
            val result = runCatching {
                val candles = repository.loadKlines(market, "15min", 160)
                require(candles.isNotEmpty()) { "No market candles returned" }
                val ticker = requireNotNull(repository.loadLatestTicker(market)) { "Live market ticker unavailable" }
                val baseSymbol = market.removeSuffix("USDT").removeSuffix("USD").ifBlank { market }
                val snapshot = MemeCoinSnapshot(symbol = baseSymbol, market = market, liquidityUsd = 0.0, marketCapUsd = 0.0, holderConcentrationPercent = null, contractVerified = null)
                val production = if (!chain.isNullOrBlank() && !contractOrMint.isNullOrBlank()) {
                    ProductionIntelligenceService().analyze(snapshot, candles, chain, contractOrMint)
                } else null
                val report = production?.coin ?: CoinIntelligenceEngine().analyze(CoinIntelligenceInput(snapshot = snapshot, candles = candles))
                val presented = CoinIntelligencePresenter.present(report, ticker.timestamp)
                Triple(presented, production, ticker.timestamp)
            }
            result.onSuccess { (ui, production, _) ->
                state = CoinIntelligenceRuntimeState(false, ui = ui, pumpDump = production?.pumpDump ?: PumpDumpDetector().analyze(repository.loadKlines(market, "15min", 160)), providerHealth = production?.providerHealth.orEmpty(), deepGaps = production?.dataGaps.orEmpty())
            }.onFailure { throwable -> state = CoinIntelligenceRuntimeState(false, error = throwable.message ?: "Market intelligence unavailable") }
        }
    }

    LaunchedEffect(market, chain, contractOrMint) {
        refresh()
        while (true) { delay(60_000); refresh() }
    }

    LazyColumn(Modifier.fillMaxSize().background(IntelBg).padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 28.dp)) {
        item {
            Column {
                Text("Coin Intelligence", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Black)
                Text(if (persian) "ارزیابی یکپارچه و قابل توضیح • داده‌های ناموجود جعل نمی‌شوند" else "Unified explainable assessment • unavailable data is never fabricated", color = IntelMuted, fontSize = 10.sp)
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(market, color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
                OutlinedButton(onClick = ::refresh, enabled = !state.loading) { Text(if (state.loading) "…" else if (persian) "بروزرسانی" else "Refresh") }
            }
        }
        state.error?.let { message -> item { Card(colors = CardDefaults.cardColors(containerColor = IntelRed.copy(alpha = .10f)), shape = RoundedCornerShape(18.dp)) { Text(message, color = IntelRed, modifier = Modifier.padding(14.dp)) } } }
        if (state.loading && state.ui == null) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
        state.ui?.let { ui ->
            item { Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = IntelPanel)) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column { Text(if (persian) "امتیاز کلی" else "Overall", color = IntelMuted, fontSize = 9.sp); Text(ui.overallLabel, color = IntelMint, fontSize = 28.sp, fontWeight = FontWeight.Black) }
                    Column(horizontalAlignment = Alignment.End) { Text(if (persian) "اعتماد" else "Confidence", color = IntelMuted, fontSize = 9.sp); Text(ui.confidenceLabel, color = IntelGold, fontSize = 20.sp, fontWeight = FontWeight.Black) }
                }
                Text(ui.verdict.replace('_', ' '), color = Color.White, fontWeight = FontWeight.Bold)
                Text(ui.summary, color = Color.White, fontSize = 12.sp)
                Text(ui.sourceLabel, color = IntelMuted, fontSize = 9.sp)
                Text("Updated: ${ui.updatedAtMs}", color = IntelMuted, fontSize = 9.sp)
            } } }
            item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { IntelMetric(if (persian) "ریسک" else "Risk", ui.riskLabel, IntelRed, Modifier.weight(1f)); IntelMetric(if (persian) "فرصت" else "Opportunity", ui.opportunityLabel, IntelGreen, Modifier.weight(1f)) } }
            state.pumpDump?.let { signal -> item { Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = IntelPanel)) { Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) { Text("Pump / Dump evidence", color = Color.White, fontWeight = FontWeight.Bold); Text("${signal.direction} • score ${signal.score.toInt()} • ${signal.severity}", color = if (signal.direction == PumpDumpDirection.PUMP) IntelGreen else IntelRed); Text("Price ${"%+.2f".format(signal.priceChangePercent)}% • volume x${"%.2f".format(signal.volumeRatio)}", color = IntelMuted, fontSize = 10.sp) } } } }
            if (state.providerHealth.isNotEmpty()) { item { IntelHeading(if (persian) "منابع واقعی" else "Verified providers") }; items(state.providerHealth) { health -> IntelLine("${health.provider}: ${if (health.ok) "OK" else health.error ?: "UNAVAILABLE"}", if (health.ok) IntelGreen else IntelGold) } }
            item { IntelHeading(if (persian) "مولفه‌های امتیاز" else "Score components") }
            items(ui.componentLabels) { label -> IntelLine(label, Color.White) }
            val gaps = (ui.gapLabels + state.deepGaps).distinct()
            if (gaps.isNotEmpty()) { item { IntelHeading(if (persian) "شکاف داده" else "Data gaps") }; items(gaps) { label -> IntelLine(label, IntelGold) }; item { Text(if (persian) "امتیاز اعتماد تا اتصال منابع تاییدشده کاهش می‌یابد." else "Confidence is reduced until verified providers supply the missing data.", color = IntelMuted, fontSize = 10.sp) } }
            if (ui.warningLabels.isNotEmpty()) { item { IntelHeading(if (persian) "هشدارها" else "Warnings") }; items(ui.warningLabels.distinct()) { label -> IntelLine(label, IntelRed) } }
        }
    }
}

@Composable private fun IntelMetric(label: String, value: String, valueColor: Color, modifier: Modifier = Modifier) { Card(modifier, shape = RoundedCornerShape(17.dp), colors = CardDefaults.cardColors(containerColor = IntelPanel2)) { Column(Modifier.padding(12.dp)) { Text(label, color = IntelMuted, fontSize = 9.sp); Text(value, color = valueColor, fontSize = 17.sp, fontWeight = FontWeight.Black) } } }
@Composable private fun IntelHeading(text: String) { Text(text, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Black) }
@Composable private fun IntelLine(text: String, color: Color) { Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = IntelPanel)) { Text(text, color = color, modifier = Modifier.padding(11.dp), fontSize = 10.sp) } }
