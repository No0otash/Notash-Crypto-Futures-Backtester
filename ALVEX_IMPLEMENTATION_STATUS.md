# ALVEX Implementation & Verification Status

This file is the live engineering ledger. A feature is only marked COMPLETE when its behavior is implemented and the CI gate passes.

## Verified in the current `main`

- ALVEX visible branding and adaptive launcher icon are present.
- Professional terminal shell is present with AI on the physical left and Settings on the physical right.
- CoinEx market ticker and OHLC loading are wired through the existing repository layer.
- Backtest terminal executes the existing backtest flow and exposes real report metrics.
- Candlestick chart supports touch selection and OHLC inspection.
- Trade markers are derived from actual report trade entry timestamps/prices.
- Pump/Dump detector is connected to supplied market candles.
- Provider-neutral whale contracts and HuntFlo message classification are present.
- Public HuntFlo Telegram adapter is present at `https://t.me/s/HuntFlo`; price-only posts are filtered.
- Research, team/investor, roadmap, tokenomics, unlock/burn/emission and on-chain data models/analyzers are present and explicitly preserve unknown data.
- Coin Intelligence consumes research, roadmap, tokenomics, unlock and on-chain inputs when supplied.
- Independent provider-neutral AI Hub is present and explicitly reports when a real AI provider is not connected.

## Android Compatibility & Responsive UI — mandatory product requirement

- 🔴 REQUIRED: The entire ALVEX application must be designed and implemented for broad Android-device compatibility, not only Xiaomi 14 or one specific screen size.
- The UI must adapt to different screen widths/heights, aspect ratios, densities, orientations and Android device configurations without clipped, overlapping or inaccessible controls.
- Navigation, charts, tables, cards, dialogs, keyboards/input fields and touch targets must remain usable on small, medium and large Android screens.
- Android lifecycle/configuration changes must not break active screens, chart state or user-entered settings where state retention is expected.
- Avoid device-specific hard-coded dimensions, coordinates or assumptions. Prefer responsive Compose layouts, adaptive sizing and density-independent units.
- Compatibility verification must include at minimum the project's supported Android SDK range and representative small/medium/large screen configurations; Xiaomi 14/Android 16 remains a test device, not the only compatibility target.
- A green compile/build alone is not sufficient evidence of device compatibility. Compatibility requires automated tests where practical plus emulator/device rendering checks for critical screens.

## Professional Trading Chart — current verification record

- 🟢 Real OHLC candlestick data: implemented from CoinEx candles; bullish/bearish bodies are rendered green/red.
- 🟢 Multiple timeframes: existing controls include `1min`, `5min`, `15min`, `1hour`, `4hour`, `1day`, and changing timeframe reloads chart data.
- 🟢 Trade-to-candle mapping model: `TradingChartModel.kt` now maps real report entry/exit timestamps to the nearest displayed candle and preserves LONG/SHORT, Entry/Exit, SL/TP, exit reason, SL-touched and PnL metadata.
- 🟢 Large-history chart data path: chart support now includes aggregation of real OHLCV candles for phone rendering instead of relying on an arbitrary 240-bar placeholder window.
- 🟡 Visual markers in the production `ProfessionalTerminal.kt` still require the final Android build plus physical-device rendering check before being called fully verified. The requested SL/TP, explicit Exit and lifecycle visuals have been specified in the build patch workflow but are not yet independently confirmed on a handset.
- 🟢 Equity Curve uses the real `BacktestReport.equityCurve` data already supplied by the backtest engine.

### Verification evidence on 2026-09-12

- TDD chart contract test was added in `app/src/test/java/com/notash/cryptobacktester/ui/TradingChartModelTest.kt`.
- The first CI run intentionally failed because the new test referenced the mapper before the implementation was added; the failure was `Unresolved reference 'buildTradingChartPoint'`.
- `TradingChartModel.kt` was then added and the official build workflow was updated so chart completion is checked as part of the normal Android build path.
- The official Android build was still running at the time of this record; no physical Android device was available in this execution environment, so handset rendering is explicitly not claimed as verified.

## CI verification

Previous clean verification run: GitHub Actions run `33393559309`.

- `gradle testDebugUnitTest`: PASS
- `gradle assembleDebug`: PASS
- APK artifact generated: PASS
- APK size observed: approximately 9.7 MB

## Known non-complete production items

These must not be represented as finished merely because the APK builds:

1. Real AI provider credentials/backend are not included; fallback AI is intentionally non-fabricating.
2. HuntFlo public-channel ingestion is implemented, but notification scheduling/preferences and a complete persisted event feed still require UI/data-store integration.
3. Some existing Strategy/Import controls are still UI-only and require connection to the actual strategy importer.
4. Settings profile/security/password/email persistence is not yet a complete account backend.
5. Five-language localization is not yet a complete resource-level translation set.
6. The current build still emits Material icon and Gradle/Actions deprecation warnings; these are warnings, not build failures.
7. The APK is below the previously requested 50 MB visual target. Size must not be artificially inflated; size should grow only from useful functionality.
8. Physical-device rendering verification for the professional trading chart is still required before the chart can be marked fully verified.
9. Broad Android compatibility is now a mandatory requirement and is not considered complete until representative small/medium/large Android configurations and critical-screen rendering have been verified.

## Rule

Do not close the ALVEX milestone as 100% complete until the known non-complete items above have been implemented and verified with tests and a clean CI build.
