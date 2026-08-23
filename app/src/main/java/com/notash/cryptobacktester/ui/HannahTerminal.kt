package com.notash.cryptobacktester.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

/**
 * Enhanced entry shell. Existing ProfessionalTerminal remains intact and is used
 * as the full professional terminal; this shell adds strategy import and Persian
 * guidance without deleting or replacing existing backtest functionality.
 */
@Composable
fun HannahTerminal() {
    val drawer = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var fa by remember { mutableStateOf(true) }
    var page by remember { mutableStateOf("terminal") }
    var strategy by remember { mutableStateOf("") }
    var selectedCoin by remember { mutableStateOf("BTCUSDT") }
    val top10 = listOf("BTCUSDT", "ETHUSDT", "SOLUSDT", "BNBUSDT", "XRPUSDT", "DOGEUSDT", "ADAUSDT", "AVAXUSDT", "LINKUSDT", "SUIUSDT")

    ModalNavigationDrawer(
        drawerState = drawer,
        drawerContent = {
            ModalDrawerSheet {
                Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(if (fa) "منوی اصلی" else "MAIN MENU", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    TextButton({ page = "terminal"; scope.launch { drawer.close() } }) { Text(if (fa) "ترمینال حرفه‌ای" else "Professional Terminal") }
                    TextButton({ page = "strategy"; scope.launch { drawer.close() } }) { Text(if (fa) "وارد کردن استراتژی / ربات تریدر" else "Import Strategy / Trader Bot") }
                    TextButton({ page = "coins"; scope.launch { drawer.close() } }) { Text(if (fa) "۱۰ ارز منتخب" else "Top 10 Selected Coins") }
                    TextButton({ fa = !fa }) { Text(if (fa) "English" else "فارسی") }
                }
            }
        }
    ) {
        Column(Modifier.fillMaxSize().padding(top = 4.dp)) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton({ scope.launch { drawer.open() } }) { Text("☰  ${if (fa) "منو" else "MENU"}", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
                Spacer(Modifier.size(8.dp))
                Text("HANNAH", fontSize = 18.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
                TextButton({ fa = !fa }) { Text(if (fa) "EN" else "FA") }
            }
            when (page) {
                "strategy" -> StrategyImportPage(fa = fa) { strategy = it }
                "coins" -> Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(if (fa) "۱۰ ارز منتخب — انتخاب برای بک‌تست" else "TOP 10 — SELECT FOR BACKTEST", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    top10.forEach { coin ->
                        Button(onClick = { selectedCoin = coin; page = "terminal" }, modifier = Modifier.fillMaxWidth()) {
                            Text(if (coin == selectedCoin) "✓ $coin" else coin)
                        }
                    }
                    Text(if (fa) "ارز انتخاب‌شده: $selectedCoin" else "Selected: $selectedCoin")
                }
                else -> ProfessionalTerminal()
            }
        }
    }
}
