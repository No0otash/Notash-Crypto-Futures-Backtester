package com.notash.cryptobacktester.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Strategy/Trader Bot import UI. Kept separate so the existing backtest engine is untouched. */
@Composable
fun StrategyImportPage(
    fa: Boolean = true,
    onStrategyReady: (String) -> Unit = {}
) {
    var code by remember { mutableStateOf("") }
    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                if (fa) "وارد کردن استراتژی / ربات تریدر" else "IMPORT STRATEGY / TRADER BOT",
                style = MaterialTheme.typography.headlineSmall
            )
        }
        item {
            Text(
                if (fa) "کد یا منطق ربات خود را وارد کنید تا برای اجرای بک‌تست آماده شود." else "Paste your strategy or trader-bot code/logic to prepare it for backtesting."
            )
        }
        item {
            OutlinedTextField(
                value = code,
                onValueChange = { code = it },
                modifier = Modifier.fillMaxWidth().height(260.dp),
                label = { Text(if (fa) "کد استراتژی / ربات" else "Strategy / Bot code") }
            )
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onStrategyReady(code) }, enabled = code.isNotBlank()) {
                    Text(if (fa) "آماده‌سازی برای بک‌تست" else "Prepare for Backtest")
                }
                Button(onClick = { code = "" }) {
                    Text(if (fa) "پاک کردن" else "Clear")
                }
            }
        }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(if (fa) "پشتیبانی" else "Supported")
                    Text(if (fa) "منطق ورود، خروج، LONG/SHORT، مدیریت ریسک و پارامترهای استراتژی می‌توانند در مرحله تبدیل به موتور بک‌تست متصل شوند." else "Entry/exit logic, LONG/SHORT, risk management and strategy parameters can be mapped into the backtest engine.")
                }
            }
        }
    }
}
