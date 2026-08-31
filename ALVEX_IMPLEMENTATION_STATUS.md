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

## CI verification

Latest verification run: GitHub Actions run `33393559309`.

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
7. The APK is below the previously requested 50 MB visual target. Size must not be artificially inflated; size should grow only from useful functionality/assets.

## Rule

Do not close the ALVEX milestone as 100% complete until the known non-complete items above have been implemented and verified with tests and a clean CI build.
