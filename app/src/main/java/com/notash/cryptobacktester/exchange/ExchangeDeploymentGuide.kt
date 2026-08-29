package com.notash.cryptobacktester.exchange

data class ExchangeDeploymentStep(
    val title: String,
    val details: String,
    val warning: String? = null
)

object ExchangeDeploymentGuide {
    fun forExchange(exchange: String): List<ExchangeDeploymentStep> = when (exchange.lowercase()) {
        "coinex" -> listOf(
            ExchangeDeploymentStep("Create API key", "Create a futures API key in the exchange account security/API section.", "Never enable withdrawal permission."),
            ExchangeDeploymentStep("Restrict permissions", "Enable only the permissions required for futures trading and data."),
            ExchangeDeploymentStep("Use IP allowlist", "If supported, restrict the key to the bot server IP address."),
            ExchangeDeploymentStep("Configure bot", "Store credentials in encrypted server-side secrets, not in the Android app."),
            ExchangeDeploymentStep("Start with demo/small size", "Validate orders, symbols, leverage, fees and stop-loss behavior before live trading.")
        )
        else -> listOf(
            ExchangeDeploymentStep("Create API key", "Use the exchange's official API management page."),
            ExchangeDeploymentStep("Restrict permissions", "Enable trading/data only; never enable withdrawals."),
            ExchangeDeploymentStep("Protect credentials", "Keep API credentials on the bot server using encrypted secrets."),
            ExchangeDeploymentStep("Test", "Run paper/demo or minimum-size tests before live deployment.")
        )
    }
}
