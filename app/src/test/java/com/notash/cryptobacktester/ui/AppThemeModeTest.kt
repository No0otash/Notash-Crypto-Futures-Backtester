package com.notash.cryptobacktester.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppThemeModeTest {
    @Test
    fun parsesPersistedThemeModeSafely() {
        assertEquals(AppThemeMode.DARK, AppThemeMode.fromStored("dark"))
        assertEquals(AppThemeMode.LIGHT, AppThemeMode.fromStored("light"))
        assertEquals(AppThemeMode.DARK, AppThemeMode.fromStored("unexpected"))
        assertEquals(AppThemeMode.DARK, AppThemeMode.fromStored(null))
    }

    @Test
    fun loginTextColorsMeetReadableContrast() {
        assertTrue(relativeContrast(0xFFF4F7FB, 0xFF101722) >= 4.5)
        assertTrue(relativeContrast(0xFF18202B, 0xFFF7F9FC) >= 4.5)
    }
}
