package com.notash.cryptobacktester.ui

import androidx.compose.runtime.Composable

/** Stable ALVEX entry surface. The professional terminal contains the production UI. */
@Composable
fun AlvexPremiumFinal(themeMode: AppThemeMode, onTheme: (AppThemeMode) -> Unit) {
    ProfessionalTerminal(themeMode, onTheme)
}
