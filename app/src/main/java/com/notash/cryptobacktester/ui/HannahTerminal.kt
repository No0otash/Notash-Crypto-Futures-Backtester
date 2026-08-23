package com.notash.cryptobacktester.ui

/**
 * Compatibility entry point for the Hannah terminal.
 * The complete professional implementation remains in ProfessionalTerminal.kt.
 * Existing callers continue to work; no project file is removed.
 */
@androidx.compose.runtime.Composable
fun HannahTerminal() {
    ProfessionalTerminal()
}
