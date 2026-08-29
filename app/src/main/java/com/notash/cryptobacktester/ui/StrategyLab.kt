package com.notash.cryptobacktester.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.notash.cryptobacktester.data.StrategyPackage
import com.notash.cryptobacktester.data.StrategyValidator
import com.notash.cryptobacktester.imports.ImportedStrategyStore
import com.notash.cryptobacktester.imports.StrategyImportParser

@Composable
fun StrategyLabPage(fa: Boolean) {
    val context = LocalContext.current
    var strategies by remember { mutableStateOf(listOf(StrategyPackage("default", "Pullback v1", "1.0"))) }
    var selected by remember { mutableStateOf(ImportedStrategyStore.activeId() ?: strategies.first().id) }
    var name by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    var importedFile by remember { mutableStateOf("") }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        try {
            val filename = uri.lastPathSegment?.substringAfterLast('/') ?: "strategy.json"
            val content = context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
                ?: throw IllegalArgumentException("فایل خوانده نشد / File could not be read")
            val imported = StrategyImportParser.parse(content, filename)
            ImportedStrategyStore.register(imported)
            val p = imported.packageData
            strategies = (strategies.filterNot { it.id == p.id } + p)
            selected = p.id
            importedFile = filename
            status = if (fa) "ربات وارد شد و آماده بک‌تست است: ${p.name}" else "Imported and ready for backtest: ${p.name}"
        } catch (e: Exception) {
            status = if (fa) "خطا در ورود فایل: ${e.message ?: "فرمت نامعتبر"}" else "Import failed: ${e.message ?: "Invalid format"}"
        }
    }

    Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(if (fa) "آزمایشگاه استراتژی" else "Strategy Lab", style = MaterialTheme.typography.headlineSmall)
        Text(if (fa) "استراتژی فعال" else "Active strategy")
        strategies.forEach { s ->
            FilterChip(selected == s.id, {
                selected = s.id
                ImportedStrategyStore.get(s.id)?.let { ImportedStrategyStore.register(it) }
            }, label = { Text("${s.name} v${s.version}") })
        }
        OutlinedTextField(name, { name = it }, label = { Text(if (fa) "نام استراتژی جدید" else "New strategy name") }, modifier = Modifier.fillMaxWidth())
        Button({
            val candidate = StrategyPackage("strategy_${strategies.size + 1}", name.ifBlank { "My Strategy ${strategies.size + 1}" }, "1.0")
            val errors = StrategyValidator.validate(candidate)
            if (errors.isEmpty()) { strategies = strategies + candidate; selected = candidate.id; name = ""; status = if (fa) "استراتژی اضافه شد؛ برای اجرای آن یک بسته واردشده یا Strategy ID معتبر لازم است." else "Strategy added; an imported package or registered Strategy ID is required to execute it." }
            else status = errors.joinToString()
        }) { Text(if (fa) "＋ افزودن استراتژی" else "＋ Add strategy") }
        Button(onClick = { picker.launch(arrayOf("application/json", "text/plain", "text/csv", "application/octet-stream")) }) {
            Text(if (fa) "📥 ورود ربات / Strategy از فایل" else "📥 Import Bot / Strategy File")
        }
        if (importedFile.isNotBlank()) Text(if (fa) "فایل: $importedFile" else "File: $importedFile")
        Text(if (fa) "فرمت امن: JSON، CSV یا TXT (key=value). کد خام ربات اجرا نمی‌شود." else "Safe formats: JSON, CSV or TXT (key=value). Raw bot code is never executed.", style = MaterialTheme.typography.bodySmall)
        if (status.isNotBlank()) Text(status)
        HorizontalDivider()
        Text(if (fa) "استراتژی انتخاب‌شده: $selected" else "Selected strategy: $selected")
        Text(if (fa) "پس از ورود، آن را در بک‌تست اجرا کنید؛ گزارش به تحلیل AI و خروجی CSV/JSON متصل است." else "After import, run it in Backtest; the report is connected to AI analysis and CSV/JSON export.")
    }
}
