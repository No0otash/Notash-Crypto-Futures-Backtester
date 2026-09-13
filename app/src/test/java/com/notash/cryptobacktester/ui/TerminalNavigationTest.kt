package com.notash.cryptobacktester.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TerminalNavigationTest {
    @Test
    fun compact_navigation_keeps_report_and_intelligence_visible_and_moves_secondary_pages_to_more() {
        val pages = terminalNavigationPages(compact = true)

        assertEquals(
            listOf(
                TerminalPage.MARKET,
                TerminalPage.MARKETS,
                TerminalPage.BACKTEST,
                TerminalPage.REPORT,
                TerminalPage.INTELLIGENCE
            ),
            pages.visible
        )
        assertEquals(listOf(TerminalPage.AI, TerminalPage.STRATEGY), pages.more)
        assertTrue(TerminalPage.REPORT !in pages.more)
        assertTrue(TerminalPage.INTELLIGENCE !in pages.more)
    }

    @Test
    fun non_compact_navigation_keeps_all_existing_pages_reachable() {
        val pages = terminalNavigationPages(compact = false)
        val all = pages.visible + pages.more

        assertEquals(TerminalPage.values().toList(), all)
    }
}
