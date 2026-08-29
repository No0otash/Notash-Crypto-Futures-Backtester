package com.notash.cryptobacktester.ai

/** Keeps generated strategies constrained to the app's supported backtest DSL. */
object StrategySafetyValidator {
    private val forbidden = listOf("API_KEY", "SECRET", "PRIVATE_KEY", "PASSWORD", "WITHDRAW", "DELETE_ACCOUNT")

    fun validate(code: String): StrategyValidationResult {
        val upper = code.uppercase()
        val errors = forbidden.filter { upper.contains(it) }.map { "Generated strategy contains forbidden token: $it" }
        val warnings = buildList {
            if (!upper.contains("STOP_LOSS")) add("No explicit stop-loss rule was detected.")
            if (!upper.contains("TAKE_PROFIT")) add("No explicit take-profit rule was detected.")
        }
        return StrategyValidationResult(errors.isEmpty(), errors, warnings)
    }
}
