package com.notash.cryptobacktester.ui

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.notash.cryptobacktester.robot.AlvexRobotImporter
import com.notash.cryptobacktester.strategy.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.zip.ZipInputStream

private val StrategyPanel = androidx.compose.ui.graphics.Color(0xFF0F1722)
private val StrategyPanel2 = androidx.compose.ui.graphics.Color(0xFF151F2C)
private val StrategyText = androidx.compose.ui.graphics.Color.White
private val StrategyMuted = androidx.compose.ui.graphics.Color(0xFF8995A8)
private val StrategyMint = androidx.compose.ui.graphics.Color(0xFF12C8B5)
private val StrategyRed = androidx.compose.ui.graphics.Color(0xFFFF6074)

private const val PREFS = "alvex_strategy_manager"
private const val HISTORY = "history"
private const val ACTIVE = "active"

@Composable
fun StrategyManagerScreen(fa: Boolean, onActiveStrategy: (ManagedStrategy) -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS, Context.MODE_PRIVATE) }
    var manager by remember { mutableStateOf(loadState(prefs)) }
    var editing by remember { mutableStateOf<ManagedStrategy?>(null) }
    var deleting by remember { mutableStateOf<ManagedStrategy?>(null) }
    var importUri by remember { mutableStateOf<Uri?>(null) }
    var notice by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {
        importUri = it
    }

    fun applyState(next: StrategyManagerState) {
        manager = next
        persist(prefs, next)
        next.history.firstOrNull { it.key == next.activeIdVersion }?.let { active ->
            registerRobot(active)
            onActiveStrategy(active)
        }
    }

    LaunchedEffect(Unit) {
        manager.history.forEach(::registerRobot)
        manager.history.firstOrNull { it.key == manager.activeIdVersion }?.let(onActiveStrategy)
    }

    LaunchedEffect(importUri) {
        val uri = importUri ?: return@LaunchedEffect
        runCatching {
            val (json, source) = readImport(context, uri)
            val robot = AlvexRobotImporter.fromJson(json)
            val imported = ManagedStrategy(
                id = robot.id,
                name = robot.name,
                version = robot.version,
                type = StrategyType.ROBOT,
                entryRules = "LONG enabled=${robot.rules.longWhenCloseAboveOpen}; SHORT enabled=${robot.rules.shortWhenCloseBelowOpen}",
                exitRules = "SL ${robot.parameters.stopLossPercent}% / TP ${robot.parameters.takeProfitPercent}%",
                riskRules = "Risk ${robot.parameters.riskPercent}%",
                timeframe = "15m",
                tradeAmount = 100.0,
                leverage = robot.parameters.leverage,
                createdAt = System.currentTimeMillis(),
                source = source,
                robotJson = json
            )
            registerRobot(imported)
            applyState(StrategyManagerCore(manager.history, manager.activeIdVersion).saveAsNewVersion(imported))
            notice = if (fa) "استراتژی با موفقیت وارد و فعال شد" else "Strategy imported and activated"
            error = null
        }.onFailure {
            error = it.message ?: "Invalid strategy file"
            notice = null
        }
        importUri = null
    }

    if (editing != null) {
        StrategyEditor(
            fa = fa,
            initial = editing!!,
            onCancel = { editing = null },
            onSave = { updated ->
                runCatching {
                    applyState(StrategyManagerCore(manager.history, manager.activeIdVersion).saveAsNewVersion(updated))
                    editing = null
                    notice = if (fa) "نسخه جدید ذخیره و فعال شد" else "New version saved and activated"
                }.onFailure { error = it.message }
            }
        )
        return
    }

    deleting?.let { target ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text(if (fa) "حذف نسخه؟" else "Delete version?") },
            text = { Text("${target.name} • ${target.version}") },
            confirmButton = {
                TextButton(onClick = {
                    applyState(StrategyManagerCore(manager.history, manager.activeIdVersion).delete(target.id, target.version, true))
                    deleting = null
                    notice = if (fa) "نسخه حذف شد" else "Version deleted"
                }) { Text(if (fa) "حذف" else "Delete") }
            },
            dismissButton = {
                TextButton(onClick = { deleting = null }) { Text(if (fa) "لغو" else "Cancel") }
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = StrategyPanel)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Strategy Manager", color = StrategyText, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                    Text(
                        if (fa) "مدیریت واقعی، نسخه‌بندی و سابقه استراتژی‌ها"
                        else "Real strategy management, versioning and history",
                        color = StrategyMuted
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { editing = newStrategy() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Outlined.Add, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text(if (fa) "استراتژی جدید" else "New Strategy")
                        }
                        OutlinedButton(
                            onClick = { picker.launch(arrayOf("application/json", "application/zip", "text/plain")) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Outlined.Add, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("Import JSON/ZIP")
                        }
                    }
                }
            }
        }
        notice?.let { text ->
            item { Text(text, color = StrategyMint, fontWeight = FontWeight.Bold) }
        }
        error?.let { text ->
            item { Text(text, color = StrategyRed, fontWeight = FontWeight.Bold) }
        }
        if (manager.history.isEmpty()) {
            item { Text(if (fa) "سابقه‌ای وجود ندارد" else "No strategy history", color = StrategyMuted) }
        }
        items(manager.history.sortedByDescending { it.createdAt }, key = { it.key }) { strategy ->
            StrategyHistoryCard(
                strategy = strategy,
                active = strategy.key == manager.activeIdVersion,
                fa = fa,
                onEdit = { editing = strategy },
                onActivate = {
                    applyState(StrategyManagerCore(manager.history, manager.activeIdVersion).activate(strategy.id, strategy.version))
                    notice = if (fa) "نسخه فعال شد" else "Version activated"
                },
                onDelete = { deleting = strategy }
            )
        }
    }
}

@Composable
private fun StrategyHistoryCard(
    strategy: ManagedStrategy,
    active: Boolean,
    fa: Boolean,
    onEdit: () -> Unit,
    onActivate: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = if (active) StrategyPanel2 else StrategyPanel)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(strategy.name, color = StrategyText, fontWeight = FontWeight.Bold)
                    Text("${strategy.type.name} • v${strategy.version} • ${strategy.timeframe}", color = StrategyMuted)
                }
                if (active) {
                    AssistChip(onClick = {}, label = { Text(if (fa) "فعال" else "ACTIVE") })
                }
            }
            Text("Entry: ${strategy.entryRules}", color = StrategyText, style = MaterialTheme.typography.bodySmall)
            Text("Exit: ${strategy.exitRules}", color = StrategyMuted, style = MaterialTheme.typography.bodySmall)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(onClick = onEdit, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Outlined.Edit, contentDescription = null)
                    Spacer(Modifier.width(3.dp))
                    Text(if (fa) "ویرایش / نسخه" else "Edit / Version")
                }
                OutlinedButton(onClick = onActivate, enabled = !active, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Outlined.Check, contentDescription = null)
                    Spacer(Modifier.width(3.dp))
                    Text(if (fa) "فعال" else "Activate")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Outlined.Delete, contentDescription = null, tint = StrategyRed)
                }
            }
        }
    }
}

@Composable
private fun StrategyEditor(
    fa: Boolean,
    initial: ManagedStrategy,
    onCancel: () -> Unit,
    onSave: (ManagedStrategy) -> Unit
) {
    var name by remember(initial.key) { mutableStateOf(initial.name) }
    var version by remember(initial.key) { mutableStateOf(initial.version) }
    var entry by remember(initial.key) { mutableStateOf(initial.entryRules) }
    var exit by remember(initial.key) { mutableStateOf(initial.exitRules) }
    var risk by remember(initial.key) { mutableStateOf(initial.riskRules) }
    var timeframe by remember(initial.key) { mutableStateOf(initial.timeframe) }
    var amount by remember(initial.key) { mutableStateOf(initial.tradeAmount.toString()) }
    var leverage by remember(initial.key) { mutableStateOf(initial.leverage.toString()) }

    LazyColumn(Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { Text(if (fa) "ویرایش / ساخت نسخه جدید" else "Edit / New Version", color = StrategyText, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black) }
        item { OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text(if (fa) "نام" else "Name") }, singleLine = true) }
        item { OutlinedTextField(version, { version = it }, Modifier.fillMaxWidth(), label = { Text("Version") }, singleLine = true) }
        item { OutlinedTextField(entry, { entry = it }, Modifier.fillMaxWidth(), label = { Text(if (fa) "قوانین ورود" else "Entry Rules") }, minLines = 2) }
        item { OutlinedTextField(exit, { exit = it }, Modifier.fillMaxWidth(), label = { Text(if (fa) "خروج / SL / TP" else "Exit / SL / TP") }, minLines = 2) }
        item { OutlinedTextField(risk, { risk = it }, Modifier.fillMaxWidth(), label = { Text(if (fa) "ریسک" else "Risk Rules") }, minLines = 2) }
        item { OutlinedTextField(timeframe, { timeframe = it }, Modifier.fillMaxWidth(), label = { Text("Timeframe") }, singleLine = true) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(amount, { amount = it }, Modifier.weight(1f), label = { Text(if (fa) "مبلغ ورود" else "Trade Amount") }, singleLine = true)
                OutlinedTextField(leverage, { leverage = it }, Modifier.weight(1f), label = { Text("Leverage") }, singleLine = true)
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) { Text(if (fa) "لغو" else "Cancel") }
                Button(
                    onClick = {
                        onSave(
                            initial.copy(
                                name = name.trim(),
                                version = version.trim(),
                                entryRules = entry.trim(),
                                exitRules = exit.trim(),
                                riskRules = risk.trim(),
                                timeframe = timeframe.trim(),
                                tradeAmount = amount.toDoubleOrNull() ?: 0.0,
                                leverage = leverage.toDoubleOrNull() ?: 0.0,
                                createdAt = System.currentTimeMillis()
                            )
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) { Text(if (fa) "ذخیره نسخه جدید" else "Save New Version") }
            }
        }
    }
}

private fun newStrategy() = ManagedStrategy(
    id = "manual_${System.currentTimeMillis()}",
    name = "New Strategy",
    version = "1.0.0",
    type = StrategyType.MANUAL,
    entryRules = "Define entry condition",
    exitRules = "Define exit / SL / TP",
    riskRules = "Risk per trade",
    timeframe = "15m",
    tradeAmount = 100.0,
    leverage = 3.0,
    createdAt = System.currentTimeMillis(),
    source = StrategySource.MANUAL
)

private fun registerRobot(strategy: ManagedStrategy) {
    strategy.robotJson?.let { json ->
        runCatching { StrategyFactory.registerImportedRobot(AlvexRobotImporter.fromJson(json)) }
    }
}

private fun loadState(prefs: android.content.SharedPreferences): StrategyManagerState {
    val raw = prefs.getString(HISTORY, null)
    val list = if (raw == null) {
        listOf(
            ManagedStrategy(
                id = "advanced_pullback_v1",
                name = "Advanced Pullback",
                version = "1.0.0",
                type = StrategyType.ROBOT,
                entryRules = "LWMA20 > LWMA50 + ATR",
                exitRules = "SL 1.5 ATR / TP 3 ATR",
                riskRules = "1% risk",
                timeframe = "15m",
                tradeAmount = 100.0,
                leverage = 3.0,
                createdAt = 1L,
                source = StrategySource.MANUAL
            )
        )
    } else {
        runCatching {
            val array = JSONArray(raw)
            (0 until array.length()).map { fromJson(array.getJSONObject(it)) }
        }.getOrDefault(emptyList())
    }
    return StrategyManagerState(list, prefs.getString(ACTIVE, list.firstOrNull()?.key))
}

private fun persist(prefs: android.content.SharedPreferences, state: StrategyManagerState) {
    val array = JSONArray()
    state.history.forEach { array.put(toJson(it)) }
    prefs.edit().putString(HISTORY, array.toString()).putString(ACTIVE, state.activeIdVersion).apply()
}

private fun toJson(strategy: ManagedStrategy) = JSONObject().apply {
    put("id", strategy.id)
    put("name", strategy.name)
    put("version", strategy.version)
    put("type", strategy.type.name)
    put("entry", strategy.entryRules)
    put("exit", strategy.exitRules)
    put("risk", strategy.riskRules)
    put("timeframe", strategy.timeframe)
    put("amount", strategy.tradeAmount)
    put("leverage", strategy.leverage)
    put("created", strategy.createdAt)
    put("source", strategy.source.name)
    put("robotJson", strategy.robotJson)
}

private fun fromJson(o: JSONObject) = ManagedStrategy(
    id = o.getString("id"),
    name = o.getString("name"),
    version = o.getString("version"),
    type = StrategyType.valueOf(o.optString("type", "ROBOT")),
    entryRules = o.optString("entry"),
    exitRules = o.optString("exit"),
    riskRules = o.optString("risk"),
    timeframe = o.optString("timeframe", "15m"),
    tradeAmount = o.optDouble("amount", 100.0),
    leverage = o.optDouble("leverage", 3.0),
    createdAt = o.optLong("created", System.currentTimeMillis()),
    source = StrategySource.valueOf(o.optString("source", "JSON")),
    robotJson = o.optString("robotJson", null)
)

private fun readImport(context: Context, uri: Uri): Pair<String, StrategySource> {
    val type = context.contentResolver.getType(uri).orEmpty()
    if (type.contains("zip") || uri.toString().endsWith(".zip", ignoreCase = true)) {
        val input = context.contentResolver.openInputStream(uri) ?: error("Unable to open ZIP")
        ZipInputStream(input).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                if (!entry.isDirectory && entry.name.endsWith(".json", ignoreCase = true)) {
                    return zip.readBytes().toString(Charsets.UTF_8) to StrategySource.ZIP
                }
                entry = zip.nextEntry
            }
        }
        error("ZIP does not contain JSON strategy")
    }
    val input = context.contentResolver.openInputStream(uri) ?: error("Unable to open file")
    return input.use { it.readBytes().toString(Charsets.UTF_8) } to StrategySource.JSON
}
