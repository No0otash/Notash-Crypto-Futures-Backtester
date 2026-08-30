package com.notash.cryptobacktester.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.notash.cryptobacktester.ai.AiHubContext
import com.notash.cryptobacktester.ai.AiHubMode
import com.notash.cryptobacktester.ai.IndependentAiHub
import kotlinx.coroutines.launch

@Composable
fun IndependentAiHubScreen() {
    var mode by remember { mutableStateOf(AiHubMode.COIN) }
    var prompt by remember { mutableStateOf("") }
    var output by remember { mutableStateOf<String?>(null) }
    var gaps by remember { mutableStateOf(emptyList<String>()) }
    val scope = rememberCoroutineScope()
    val hub = remember { IndependentAiHub() }
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Text("NOTASH // INDEPENDENT AI HUB", style = MaterialTheme.typography.headlineSmall)
            Text("Provider-neutral workspace • no guaranteed predictions • source/data-gap aware")
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf(AiHubMode.CHAT, AiHubMode.COIN, AiHubMode.STRATEGY, AiHubMode.TRADE, AiHubMode.RISK, AiHubMode.RESEARCH, AiHubMode.EDUCATION).forEach {
                    FilterChip(selected = mode == it, onClick = { mode = it }, label = { Text(it.name) })
                }
            }
        }
        item {
            OutlinedTextField(prompt, { prompt = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Question / symbol / trade") })
            Button(onClick = {
                scope.launch {
                    val report = when (mode) {
                        AiHubMode.CHAT -> hub.chat(prompt)
                        AiHubMode.COIN -> hub.analyzeCoin(prompt.ifBlank { "BTCUSDT" }, AiHubContext())
                        AiHubMode.STRATEGY -> hub.analyzeStrategy(prompt, AiHubContext())
                        AiHubMode.TRADE -> hub.analyzeTrade(prompt, AiHubContext())
                        AiHubMode.RISK -> hub.analyzeRisk(AiHubContext())
                        AiHubMode.RESEARCH -> hub.research(prompt, AiHubContext())
                        AiHubMode.EDUCATION -> hub.educate(prompt, com.notash.cryptobacktester.ai.EducationLevel.SIMPLE)
                    }
                    output = report.narrative
                    gaps = report.dataGaps
                }
            }) { Text("Analyze") }
        }
        output?.let { text -> item { Card { Column(Modifier.padding(12.dp)) { Text(text); Text("Real AI connected: ${if (text.startsWith("AI provider is not connected")) "NO" else "YES"}") } } } }
        if (gaps.isNotEmpty()) {
            item { Text("DATA GAPS", style = MaterialTheme.typography.titleMedium) }
            items(gaps) { Text("• $it") }
        }
    }
}
