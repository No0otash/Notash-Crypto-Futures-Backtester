package com.notash.cryptobacktester.deploy

data class DeploymentStep(val title: String, val details: String, val warning: String? = null)

object ExchangeDeploymentGuide {
    fun steps(exchange: String): List<DeploymentStep> = listOf(
        DeploymentStep("1. Create API key", "Create an API key in the $exchange account settings."),
        DeploymentStep("2. Futures permission", "Enable only the futures/trading permission required by the bot."),
        DeploymentStep("3. Disable withdrawals", "Never grant withdrawal permission to a trading bot."),
        DeploymentStep("4. Restrict IP", "Use IP allowlisting when the exchange supports it."),
        DeploymentStep("5. Test first", "Run the strategy on testnet, demo or paper trading before live deployment."),
        DeploymentStep("6. Start small", "Use the smallest practical position size and conservative leverage."),
        DeploymentStep("7. Kill switch", "Keep an emergency stop mechanism available outside the bot."),
        DeploymentStep("8. Monitor", "Review fills, fees, funding, slippage, errors and drawdown after deployment.")
    )
}
