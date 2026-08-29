package com.notash.cryptobacktester

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { App() }
    }
}

data class Candle(val time: Long, val open: Double, val high: Double, val low: Double, val close: Double, val volume: Double)
data class BacktestSettings(val initialBalance: Double = 1000.0, val riskPercent: Double = 1.0, val leverage: Double = 10.0, val fastLwma: Int = 20, val slowLwma: Int = 50, val atrPeriod: Int = 14, val entryOffsetAtr: Double = .5, val slAtr: Double = 1.5, val tpAtr: Double = 3.0, val makerFee: Double = .0002, val takerFee: Double = .0005, val slippage: Double = .0001)
data class TradeResult(val side: String, val entry: Double, val exit: Double, val qty: Double, val pnl: Double, val funding: Double, val fees: Double, val reason: String)
data class BacktestReport(val endingBalance: Double, val pnl: Double, val roi: Double, val trades: List<TradeResult>, val maxDrawdown: Double)

@Composable
fun App() {
    var showAiHub by remember { mutableStateOf(false) }
    val bg = Color(0xFF070A12)
    val panel = Color(0xFF101728)
    val cyan = Color(0xFF00D9FF)

    MaterialTheme(colorScheme = darkColorScheme(background = bg, surface = panel)) {
        if (showAiHub) {
            AiHubEntryScreen(onBack = { showAiHub = false }, bg = bg, panel = panel, cyan = cyan)
        } else {
            DashboardScreen(onOpenAi = { showAiHub = true }, bg = bg, panel = panel, cyan = cyan)
        }
    }
}

@Composable
private fun DashboardScreen(onOpenAi: () -> Unit, bg: Color, panel: Color, cyan: Color) {
    var market by remember { mutableStateOf("BTCUSDT") }
    var initial by remember { mutableStateOf("1000") }
    var risk by remember { mutableStateOf("1") }
    var leverage by remember { mutableStateOf("10") }
    var ltf by remember { mutableStateOf("15min") }
    var htf by remember { mutableStateOf("1hour") }
    var status by remember { mutableStateOf("READY") }

    Box(Modifier.fillMaxSize().background(bg)) {
        LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Text("NOTASH // CRYPTO LAB", color = cyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("Futures Backtester", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                Text("Multi-Exchange • Crypto Native", color = Color.Gray)
            }
            item {
                Card(colors = CardDefaults.cardColors(containerColor = panel)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                        Text("MARKET CONFIG", color = cyan, fontWeight = FontWeight.Bold)
                        OutlinedTextField(market, { market = it }, label = { Text("Market") }, singleLine = true)
                        OutlinedTextField(ltf, { ltf = it }, label = { Text("LTF") }, singleLine = true)
                        OutlinedTextField(htf, { htf = it }, label = { Text("HTF") }, singleLine = true)
                        OutlinedTextField(initial, { initial = it }, label = { Text("Initial Balance USDT") }, singleLine = true)
                        OutlinedTextField(risk, { risk = it }, label = { Text("Risk %") }, singleLine = true)
                        OutlinedTextField(leverage, { leverage = it }, label = { Text("Leverage") }, singleLine = true)
                        Button(onClick = { status = "BACKTEST ENGINE READY — DATA ADAPTER PENDING" }, modifier = Modifier.fillMaxWidth()) { Text("RUN BACKTEST") }
                    }
                }
            }
            item {
                Card(colors = CardDefaults.cardColors(containerColor = panel)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("AI INTELLIGENCE", color = cyan, fontWeight = FontWeight.Bold)
                        Text("Ask Hannah about trading, backtesting, strategies, risk, crypto and market concepts.", color = Color.LightGray)
                        Button(onClick = onOpenAi, modifier = Modifier.fillMaxWidth()) { Text("OPEN AI HUB") }
                    }
                }
            }
            item {
                Card(colors = CardDefaults.cardColors(containerColor = panel)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("STRATEGY", color = cyan, fontWeight = FontWeight.Bold)
                        Text("HTF LWMA 20/50 • LTF LWMA 20 • ATR 14")
                        Text("Limit Entry 0.5 ATR • SL 1.5 ATR • TP 3 ATR")
                    }
                }
            }
            item { Text(status, color = Color.LightGray, fontSize = 12.sp) }
        }
    }
}

@Composable
private fun AiHubEntryScreen(onBack: () -> Unit, bg: Color, panel: Color, cyan: Color) {
    var question by remember { mutableStateOf("") }
    var answer by remember { mutableStateOf("Ask Hannah anything about crypto, trading, backtesting, risk or strategy design.") }

    Box(Modifier.fillMaxSize().background(bg)) {
        LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("HANNAH // AI HUB", color = cyan, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    TextButton(onClick = onBack) { Text("BACK") }
                }
            }
            item {
                Card(colors = CardDefaults.cardColors(containerColor = panel)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("INDEPENDENT AI ASSISTANT", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Education, strategy explanations, crypto concepts and backtest guidance.", color = Color.Gray)
                        OutlinedTextField(question, { question = it }, label = { Text("Your question") }, modifier = Modifier.fillMaxWidth(), minLines = 4)
                        Button(onClick = { answer = if (question.isBlank()) "Please enter a question." else "Hannah AI context prepared for: $question" }, modifier = Modifier.fillMaxWidth()) { Text("ASK HANNAH") }
                    }
                }
            }
            item {
                Card(colors = CardDefaults.cardColors(containerColor = panel)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("ANSWER", color = cyan, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Text(answer, color = Color.White)
                    }
                }
            }
            item { Text("AI responses are evidence-aware; missing data is not treated as a positive signal.", color = Color.Gray, fontSize = 12.sp) }
        }
    }
}
