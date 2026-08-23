package com.notash.cryptobacktester.ui

/**
 * Backwards-compatible entry point for the previous professional terminal.
 *
 * The professional UI is now implemented by HannahTerminal. Keeping this
 * composable avoids breaking any existing references to ProfessionalTerminal.
 */
@androidx.compose.runtime.Composable
fun ProfessionalTerminal() {
    HannahTerminal()
}
