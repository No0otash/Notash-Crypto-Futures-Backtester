package com.notash.cryptobacktester.ui

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

enum class TerminalPage(val titleFa: String, val titleEn: String) {
    MARKET("بازار", "Market"),
    FUTURES("فیوچرز", "Futures"),
    BACKTEST("بک‌تست", "Backtest"),
    AI("تحلیل هوشمند", "AI Analyst"),
    TRADES("معاملات", "Trades"),
    SETTINGS("تنظیمات", "Settings")
}

@Composable
fun TerminalNavigation(selected: TerminalPage, onSelected: (TerminalPage) -> Unit, persian: Boolean = true) {
    NavigationBar {
        TerminalPage.values().forEach { page ->
            NavigationBarItem(
                selected = selected == page,
                onClick = { onSelected(page) },
                icon = { Text(page.name.take(1)) },
                label = { Text(if (persian) page.titleFa else page.titleEn) }
            )
        }
    }
}
