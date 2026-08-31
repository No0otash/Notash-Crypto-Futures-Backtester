package com.notash.cryptobacktester.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoGraph
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

enum class TerminalPage(val titleFa: String, val titleEn: String) {
    MARKET("خانه", "Home"),
    MARKETS("بازار", "Markets"),
    BACKTEST("بک‌تست", "Backtest"),
    STRATEGY("استراتژی", "Strategy"),
    INTELLIGENCE("هوش بازار", "Intel"),
    AI("AI Hub", "AI Hub")
}

@Composable
fun TerminalNavigation(selected: TerminalPage, onSelected: (TerminalPage) -> Unit, persian: Boolean = true) {
    NavigationBar {
        val pages = listOf(
            TerminalPage.MARKET to Icons.Outlined.Home,
            TerminalPage.MARKETS to Icons.Outlined.ShowChart,
            TerminalPage.BACKTEST to Icons.Outlined.AutoGraph,
            TerminalPage.STRATEGY to Icons.Outlined.Science,
            TerminalPage.INTELLIGENCE to Icons.Outlined.Psychology,
            TerminalPage.AI to Icons.Outlined.Psychology
        )
        pages.forEach { (page, icon) ->
            NavigationBarItem(
                selected = selected == page,
                onClick = { onSelected(page) },
                icon = { Icon(icon, contentDescription = if (persian) page.titleFa else page.titleEn) },
                label = { Text(if (persian) page.titleFa else page.titleEn) }
            )
        }
    }
}
