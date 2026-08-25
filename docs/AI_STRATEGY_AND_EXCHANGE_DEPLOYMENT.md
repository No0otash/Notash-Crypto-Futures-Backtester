# Hannah AI Strategy Workflow

The product roadmap is:

1. User describes a strategy in Persian or English.
2. Secure AI gateway converts the request into `AiStrategySpec` and a reviewable strategy DSL/code representation.
3. Hannah validates the strategy before execution. Arbitrary generated code is never executed directly.
4. The strategy is registered/imported into the existing strategy system.
5. User chooses exchange, symbol, timeframe, capital, leverage, fees and funding assumptions.
6. Backtest runs and produces trade-by-trade results.
7. AI receives the structured backtest report and diagnoses weaknesses, then proposes measurable changes.
8. User can version, compare and re-test strategies.

## Exchange deployment

The app should provide a broker/exchange guide rather than silently trading with user credentials.

For each supported exchange the guide should show:
- API key creation
- Secret/key permissions
- IP allow-list recommendation
- Futures permission requirements
- Testnet/demo availability when supported
- How to disable withdrawal permission
- How to start the bot in paper/demo mode
- How to monitor positions, orders, SL/TP and logs

Credentials must be stored in platform secure storage and never sent to the AI model. Production order execution should be performed by a separate signed trading service with rate limits, risk limits, audit logs and an emergency kill switch.

## AI connection

The Android app must call a secure backend AI gateway. The model API key must remain server-side. The gateway should authenticate the Hannah user, enforce quotas, validate the structured output, and log only the minimum required telemetry.
