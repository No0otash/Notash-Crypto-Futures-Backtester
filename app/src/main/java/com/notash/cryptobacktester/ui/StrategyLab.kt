package com.notash.cryptobacktester.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.notash.cryptobacktester.data.StrategyPackage
import com.notash.cryptobacktester.data.StrategyValidator

@Composable
fun StrategyLabPage(fa: Boolean) {
    var strategies by remember { mutableStateOf(listOf(StrategyPackage("default", "Pullback v1", "1.0"))) }
    var selected by remember { mutableStateOf(strategies.first().id) }
    var name by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(if (fa) "آزمایشگاه استراتژی" else "Strategy Lab", style = MaterialTheme.typography.headlineSmall)
        Text(if (fa) "استراتژی فعال" else "Active strategy")
        strategies.forEach { s ->
            FilterChip(selected == s.id, { selected = s.id }, label = { Text("${s.name} v${s.version}") })
        }
        OutlinedTextField(name, { name = it }, label = { Text(if (fa) "نام استراتژی جدید" else "New strategy name") }, modifier = Modifier.fillMaxWidth())
        Button({
            val candidate = StrategyPackage("strategy_${strategies.size + 1}", name.ifBlank { "My Strategy ${strategies.size + 1}" }, "1.0")
            val errors = StrategyValidator.validate(candidate)
            if (errors.isEmpty()) { strategies = strategies + candidate; selected = candidate.id; name = ""; status = if (fa) "استراتژی اضافه و فعال شد" else "Strategy added and activated" }
            else status = errors.joinToString()
        }) { Text(if (fa) "＋ افزودن استراتژی" else "＋ Add strategy") }
        OutlinedButton({ status = if (fa) "فایل Strategy باید به قالب امن StrategyPackage تبدیل و اعتبارسنجی شود." else "Strategy files must be converted to the safe StrategyPackage format and validated." }) { Text(if (fa) "📤 ورود ربات / Strategy" else "📤 Import Bot / Strategy") }
        if (status.isNotBlank()) Text(status)
        HorizontalDivider()
        Text(if (fa) "همین Strategy را می‌توان برای Historical یا Live Backtest انتخاب کرد." else "The selected strategy can be used for Historical or Live Backtest.")
    }
}
