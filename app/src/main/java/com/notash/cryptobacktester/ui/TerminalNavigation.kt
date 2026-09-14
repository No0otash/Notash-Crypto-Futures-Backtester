package com.notash.cryptobacktester.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.AutoGraph
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp

enum class TerminalPage(val titleFa: String, val titleEn: String) {
    MARKET("خانه", "Home"),
    MARKETS("بازارها", "Markets"),
    BACKTEST("بک‌تست", "Backtest"),
    REPORT("گزارش معاملات", "Trade Report"),
    STRATEGY("استراتژی", "Strategy"),
    INTELLIGENCE("هوش بازار", "Intelligence"),
    AI("AI Hub", "AI Hub")
}

data class TerminalNavigationPages(
    val visible: List<TerminalPage>,
    val more: List<TerminalPage>
)

fun terminalNavigationPages(compact: Boolean): TerminalNavigationPages {
    val ordered = TerminalPage.values().toList()
    if (!compact) return TerminalNavigationPages(visible = ordered, more = emptyList())

    val visible = listOf(
        TerminalPage.MARKET,
        TerminalPage.MARKETS,
        TerminalPage.BACKTEST,
        TerminalPage.REPORT,
        TerminalPage.INTELLIGENCE
    )
    val more = listOf(TerminalPage.AI, TerminalPage.STRATEGY)
    return TerminalNavigationPages(visible = visible, more = more)
}

@Composable
fun TerminalNavigation(
    selected: TerminalPage,
    onSelected: (TerminalPage) -> Unit,
    persian: Boolean = true
) {
    BoxWithConstraints {
        val compact = maxWidth < 600.dp
        val pages = terminalNavigationPages(compact)
        val icons = mapOf(
            TerminalPage.MARKET to Icons.Outlined.Home,
            TerminalPage.MARKETS to Icons.Outlined.ShowChart,
            TerminalPage.BACKTEST to Icons.Outlined.AutoGraph,
            TerminalPage.REPORT to Icons.Outlined.ShowChart,
            TerminalPage.INTELLIGENCE to Icons.Outlined.Psychology,
            TerminalPage.AI to Icons.Outlined.AutoAwesome,
            TerminalPage.STRATEGY to Icons.Outlined.Build
        )

        NavigationBar {
            pages.visible.forEach { page ->
                NavigationBarItem(
                    selected = selected == page,
                    onClick = { onSelected(page) },
                    icon = { Icon(icons.getValue(page), contentDescription = if (persian) page.titleFa else page.titleEn) },
                    label = { Text(if (persian) page.titleFa else page.titleEn, maxLines = 1) }
                )
            }

            if (pages.more.isNotEmpty()) {
                var moreExpanded by remember { mutableStateOf(false) }
                val moreSelected = selected in pages.more
                NavigationBarItem(
                    selected = moreSelected,
                    onClick = { moreExpanded = true },
                    icon = {
                        Box {
                            Icon(Icons.Outlined.MoreVert, contentDescription = if (persian) "بیشتر" else "More")
                            DropdownMenu(
                                expanded = moreExpanded,
                                onDismissRequest = { moreExpanded = false }
                            ) {
                                pages.more.forEach { page ->
                                    DropdownMenuItem(
                                        text = { Text(if (persian) page.titleFa else page.titleEn) },
                                        onClick = {
                                            moreExpanded = false
                                            onSelected(page)
                                        },
                                        leadingIcon = { Icon(icons.getValue(page), contentDescription = null) }
                                    )
                                }
                            }
                        }
                    },
                    label = { Text(if (persian) "بیشتر" else "More", maxLines = 1) }
                )
            }
        }
    }
}
