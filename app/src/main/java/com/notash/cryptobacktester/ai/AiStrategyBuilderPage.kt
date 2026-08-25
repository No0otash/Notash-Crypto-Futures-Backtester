package com.notash.cryptobacktester.ai

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val aiPanel = Color(0xFF0B1020)
private val aiCyan = Color(0xFF22D3EE)
private val aiText = Color(0xFFF5F7FF)
private val aiMuted = Color(0xFF8490A7)

@Composable
fun AiStrategyBuilderPage(
    fa: Boolean,
    result: StrategyGenerationResult? = null,
    isGenerating: Boolean = false,
    error: String? = null,
    onGenerate: (StrategyGenerationRequest) -> Unit,
    onUseForBacktest: (StrategyGenerationResult) -> Unit,
    onSave: (StrategyGenerationResult) -> Unit
) {
    var prompt by remember { mutableStateOf("") }
    LazyColumn(Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Text(if (fa) "ساخت استراتژی با هوش مصنوعی" else "AI STRATEGY BUILDER", color = aiCyan, fontSize = 24.sp) }
        item { Text(if (fa) "استراتژی را با زبان طبیعی توضیح بده؛ قبل از بک‌تست اعتبارسنجی می‌شود." else "Describe the strategy in natural language; it is validated before backtesting.", color = aiMuted, fontSize = 10.sp) }
        item { OutlinedTextField(prompt, { prompt = it }, Modifier.fillMaxWidth().height(170.dp), label = { Text(if (fa) "استراتژی مورد نظر شما" else "Describe your strategy") }) }
        item { Button(onClick = { onGenerate(StrategyGenerationRequest(prompt = prompt, language = if (fa) "fa" else "en")) }, enabled = prompt.isNotBlank() && !isGenerating, modifier = Modifier.fillMaxWidth()) { Text(if (isGenerating) (if (fa) "در حال ساخت…" else "GENERATING…") else if (fa) "تبدیل به استراتژی قابل بک‌تست" else "GENERATE TESTABLE STRATEGY") } }
        error?.let { message -> item { Text(message, color = Color(0xFFFF5577), fontSize = 10.sp) } }
        result?.let { r ->
            val s = r.strategy
            item { Card(colors = CardDefaults.cardColors(containerColor = aiPanel)) { Column(Modifier.padding(14.dp)) {
                Text(s.name, color = aiText, fontSize = 18.sp)
                Text(if (fa) s.explanationFa else s.explanationEn, color = aiMuted, fontSize = 11.sp)
                Text(if (fa) "ورود: ${s.entryRules.joinToString(" | ")}" else "Entry: ${s.entryRules.joinToString(" | ")}", color = aiText, fontSize = 10.sp)
                Text(if (fa) "خروج: ${s.exitRules.joinToString(" | ")}" else "Exit: ${s.exitRules.joinToString(" | ")}", color = aiText, fontSize = 10.sp)
                Text("SL ${s.risk.stopLossAtr ?: "—"} ATR • TP ${s.risk.takeProfitAtr ?: "—"} ATR • Risk ${s.risk.riskPercent}% • ${s.risk.leverage}x", color = aiCyan, fontSize = 10.sp)
                r.warnings.forEach { Text("⚠ $it", color = Color(0xFFFFB84D), fontSize = 9.sp) }
            } } }
            item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) { Button(onClick = { onUseForBacktest(r) }, modifier = Modifier.weight(1f)) { Text(if (fa) "بک‌تست" else "BACKTEST") }; OutlinedButton(onClick = { onSave(r) }, modifier = Modifier.weight(1f)) { Text(if (fa) "ذخیره نسخه" else "SAVE VERSION") } } }
        }
    }
}
