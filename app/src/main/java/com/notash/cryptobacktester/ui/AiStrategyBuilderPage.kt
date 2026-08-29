package com.notash.cryptobacktester.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notash.cryptobacktester.ai.StrategyGenerationRequest
import com.notash.cryptobacktester.ai.StrategyGenerationResult
import com.notash.cryptobacktester.ai.StrategyPackageValidator

private val AiPanel = Color(0xFF0B1020)
private val AiText = Color(0xFFF5F7FF)
private val AiMuted = Color(0xFF8490A7)
private val AiCyan = Color(0xFF22D3EE)
private val AiGreen = Color(0xFF20E6A5)
private val AiRed = Color(0xFFFF5577)

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
    var symbol by remember { mutableStateOf("BTCUSDT") }
    var timeframe by remember { mutableStateOf("1h") }

    LazyColumn(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Text(if (fa) "ساخت استراتژی با هوش مصنوعی" else "AI STRATEGY BUILDER", color = AiCyan, fontSize = 25.sp, fontWeight = FontWeight.Black)
            Text(if (fa) "استراتژی را با زبان طبیعی توضیح بده؛ AI آن را به قوانین قابل بک‌تست تبدیل می‌کند." else "Describe the strategy naturally; AI converts it into testable machine-readable rules.", color = AiMuted, fontSize = 11.sp)
        }
        item { Card(colors = CardDefaults.cardColors(containerColor = AiPanel)) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(prompt, { prompt = it }, Modifier.fillMaxWidth(), minLines = 5, label = { Text(if (fa) "استراتژی مورد نظر شما" else "Describe your strategy") }, placeholder = { Text(if (fa) "مثلاً: وقتی روند 4 ساعته صعودی است و در 15 دقیقه قیمت به EMA20 برمی‌گردد..." else "Example: when the 4h trend is bullish and price pulls back to EMA20 on 15m...") })
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    OutlinedTextField(symbol, { symbol = it }, Modifier.weight(1f), singleLine = true, label = { Text("Symbol") })
                    OutlinedTextField(timeframe, { timeframe = it }, Modifier.weight(1f), singleLine = true, label = { Text("Timeframe") })
                }
                Button(onClick = { onGenerate(StrategyGenerationRequest(prompt, symbol, timeframe, if (fa) "fa" else "en")) }, enabled = prompt.isNotBlank() && !isGenerating, modifier = Modifier.fillMaxWidth()) {
                    Text(if (isGenerating) (if (fa) "در حال ساخت…" else "Generating…") else (if (fa) "تبدیل به استراتژی" else "GENERATE STRATEGY"))
                }
            }
        } }
        error?.let { item { Text(it, color = AiRed, fontSize = 11.sp) } }
        result?.let { generated ->
            val validation = StrategyPackageValidator.validate(generated.strategy)
            item { Card(colors = CardDefaults.cardColors(containerColor = AiPanel)) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(generated.strategy.name, color = AiText, fontSize = 18.sp, fontWeight = FontWeight.Black)
                    Text("${generated.strategy.symbol} • ${generated.strategy.timeframe} • ${generated.strategy.direction}", color = AiCyan, fontSize = 10.sp)
                    Text(if (fa) "قوانین ورود" else "ENTRY RULES", color = AiMuted, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                    generated.strategy.entryRules.forEach { Text("• $it", color = AiText, fontSize = 11.sp) }
                    Text(if (fa) "قوانین خروج" else "EXIT RULES", color = AiMuted, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                    generated.strategy.exitRules.forEach { Text("• $it", color = AiText, fontSize = 11.sp) }
                    Text(if (fa) "ریسک: ${generated.strategy.risk.riskPercent}% • اهرم: ${generated.strategy.risk.leverage}x" else "Risk: ${generated.strategy.risk.riskPercent}% • Leverage: ${generated.strategy.risk.leverage}x", color = AiMuted, fontSize = 10.sp)
                    if (validation.isEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        Text(if (fa) "✓ استراتژی معتبر است" else "✓ Strategy validated", color = AiGreen, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                            Button(onClick = { onUseForBacktest(generated) }, modifier = Modifier.weight(1f)) { Text(if (fa) "بک‌تست" else "BACKTEST") }
                            Button(onClick = { onSave(generated) }, modifier = Modifier.weight(1f)) { Text(if (fa) "ذخیره نسخه" else "SAVE VERSION") }
                        }
                    } else validation.forEach { Text("⚠ $it", color = AiRed, fontSize = 10.sp) }
                }
            } }
        }
    }
}
