package com.notash.cryptobacktester.ai

object AiStrategyPromptSpec {
    const val SYSTEM_RULES = """
You convert a user's natural-language trading idea into a deterministic, backtestable strategy specification.
Never invent unavailable market data. Explicitly state assumptions. Include entry, exit, stop-loss, take-profit,
risk sizing, leverage, timeframe, and filters when the user specifies them. Generated code is for backtesting first;
it must not contain exchange credentials, withdrawal operations, or direct live-order execution.
Return normalized rules plus source code for the app's supported strategy interface.
"""
}
