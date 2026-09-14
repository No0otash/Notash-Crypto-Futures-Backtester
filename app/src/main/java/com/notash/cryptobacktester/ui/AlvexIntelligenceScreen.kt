package com.notash.cryptobacktester.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.notash.cryptobacktester.intelligence.CoinExMarketIntelligenceDataSource
import com.notash.cryptobacktester.intelligence.MemeMarketScanner
import com.notash.cryptobacktester.intelligence.MemeScanResult
import com.notash.cryptobacktester.intelligence.MarketIntelligenceScanner
import com.notash.cryptobacktester.intelligence.RadarPumpDumpDirection
import com.notash.cryptobacktester.intelligence.RadarPumpDumpSignal

@Composable
fun AlvexIntelligenceScreen(fa: Boolean, market: String) {
    val source = remember { CoinExMarketIntelligenceDataSource() }
    val pumpScanner = remember { MarketIntelligenceScanner(source) }
    val memeScanner = remember { MemeMarketScanner(source) }
    var state by remember { mutableStateOf<IntelligenceState>(IntelligenceState.Loading) }
    var tab by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        state = IntelligenceState.Loading
        runCatching {
            val pump = pumpScanner.scanPumpDump()
            val meme = memeScanner.scan()
            IntelligenceState.Success(pump.signals, meme, pump.scannedMarkets, pump.unavailableMarkets, pump.scannedAtMs)
        }.onSuccess { state = it }
            .onFailure { state = IntelligenceState.Error(it.message ?: if (fa) "خطا در دریافت داده بازار" else "Market data error") }
    }

    LazyColumn(Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Text(if (fa) "هوش بازار ALVEX" else "ALVEX Market Intelligence", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
            Text(if (fa) "اسکن واقعی بازار فیوچرز CoinEx؛ بدون لیست ثابت" else "Live CoinEx futures scan; no fixed symbol list", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = tab == 0, onClick = { tab = 0 }, label = { Text("Pump / Dump") })
                FilterChip(selected = tab == 1, onClick = { tab = 1 }, label = { Text("Meme / Shitcoin") })
            }
        }
        when (val current = state) {
            IntelligenceState.Loading -> item { CircularProgressIndicator() }
            is IntelligenceState.Error -> item { StatusCard(current.message) }
            is IntelligenceState.Empty -> item { StatusCard(current.reason) }
            is IntelligenceState.Offline -> item { StatusCard(if (fa) "داده آفلاین/قدیمی است" else "Offline or stale data") }
            is IntelligenceState.Success -> {
                item { ScanSummary(current, fa) }
                if (tab == 0) items(current.pumpDump) { PumpSignalCard(it, fa) }
                else items(current.meme) { MemeSignalCard(it, fa) }
            }
        }
    }
}

@Composable private fun ScanSummary(state: IntelligenceState.Success, fa: Boolean) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.padding(13.dp)) {
            Text(if (fa) "اسکن واقعی" else "REAL SCAN", fontWeight = FontWeight.Bold)
            Text(if (fa) "${state.scannedMarkets} بازار بررسی شد؛ ${state.unavailableMarkets} بازار داده کامل نداشت" else "${state.scannedMarkets} markets scanned; ${state.unavailableMarkets} unavailable", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable private fun PumpSignalCard(signal: RadarPumpDumpSignal, fa: Boolean) {
    val direction = when (signal.direction) {
        RadarPumpDumpDirection.PUMP -> "PUMP"
        RadarPumpDumpDirection.DUMP -> "DUMP"
        RadarPumpDumpDirection.WATCH -> "WATCH"
    }
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(signal.symbol, fontWeight = FontWeight.Black)
                AssistChip(onClick = {}, label = { Text(direction) })
            }
            Text(if (fa) "امتیاز ${signal.score} • اطمینان ${signal.confidence}%" else "Score ${signal.score} • Confidence ${signal.confidence}%")
            Text(signal.reasons.take(3).joinToString(" • "), color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (signal.dataGaps.isNotEmpty()) Text("Data gaps: ${signal.dataGaps.take(4).joinToString()}", color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable private fun MemeSignalCard(result: MemeScanResult, fa: Boolean) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(result.symbol, fontWeight = FontWeight.Black)
                Text("${result.opportunityScore.toInt()} / 100", fontWeight = FontWeight.Bold)
            }
            Text(if (result.dataComplete) result.message else if (fa) "داده نقدینگی/مارکت‌کپ موجود نیست؛ امتیاز موقت است" else "Liquidity/market-cap data unavailable; score is provisional", color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (result.flags.isNotEmpty()) Text(result.flags.joinToString(" • "), color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable private fun StatusCard(message: String) { Card(Modifier.fillMaxWidth()) { Text(message, Modifier.padding(16.dp)) } }
