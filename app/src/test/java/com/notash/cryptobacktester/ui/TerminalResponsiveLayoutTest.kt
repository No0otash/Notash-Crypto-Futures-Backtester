package com.notash.cryptobacktester.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class TerminalResponsiveLayoutTest {
    @Test
    fun compact_width_uses_compact_terminal_layout() {
        assertEquals(TerminalLayoutMode.COMPACT, terminalLayoutMode(360))
        assertEquals(TerminalLayoutMode.COMPACT, terminalLayoutMode(599))
    }

    @Test
    fun standard_and_expanded_widths_use_stable_layout_modes() {
        assertEquals(TerminalLayoutMode.STANDARD, terminalLayoutMode(600))
        assertEquals(TerminalLayoutMode.STANDARD, terminalLayoutMode(839))
        assertEquals(TerminalLayoutMode.EXPANDED, terminalLayoutMode(840))
    }

    @Test
    fun breakpoints_are_monotonic_for_phone_tablet_and_large_screens() {
        assertEquals(TerminalLayoutMode.COMPACT, terminalLayoutMode(411))
        assertEquals(TerminalLayoutMode.STANDARD, terminalLayoutMode(768))
        assertEquals(TerminalLayoutMode.EXPANDED, terminalLayoutMode(1024))
    }
}
