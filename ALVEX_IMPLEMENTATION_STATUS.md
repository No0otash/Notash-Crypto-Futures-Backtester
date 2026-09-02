# ALVEX Implementation & Verification Status

This file is the live engineering ledger. A feature is only marked COMPLETE when its behavior is implemented and the CI gate passes.

## Verified in current main

- ALVEX visible branding and adaptive launcher icon are present.
- Professional terminal shell is present with AI on the physical left and Settings on the physical right.
- CoinEx market ticker and OHLC loading are wired through the repository layer.
- Backtest terminal executes the existing backtest flow and exposes real report metrics.
- Candlestick chart supports touch selection and OHLC inspection.
- Trade markers are derived from actual report trade entry timestamps/prices.
- Pump/Dump detector is connected to supplied market candles.
- Provider-neutral whale contracts and HuntFlo message classification are present.
- Public HuntFlo Telegram adapter is present; price-only posts are filtered.
- Research, team/investor, roadmap, tokenomics, unlock/burn/emission and on-chain data models/analyzers are present.
- Coin Intelligence consumes research, roadmap, tokenomics, unlock and on-chain inputs when supplied.
- Independent provider-neutral AI Hub is present and explicitly reports when a real AI provider is not connected.

## Production hardening added on `feature/alvex-production-hardening`

- `HttpAiProvider`: real HTTPS AI-provider boundary; endpoint/key are injected and no secret is stored in source.
- `NotificationPreferences`: persisted Whale/Pump-Dump/Meme-Risk/News switches and minimum Whale threshold.
- `AlvexNotificationPolicy`: deterministic notification gating based on persisted user policy.
- `SecureAccountStore`: local email plus non-reversible salted password verifier; plaintext passwords are never persisted.
- `StrategyPackageImporter`: executable strict JSON import with validation through the existing `StrategyValidator`.
- `AppLanguage`: canonical English source plus Persian, Arabic, French and Chinese language identifiers.
- `LiveBacktestRunner`: runs the existing `BacktestEngine` against freshly fetched CoinEx Futures candles rather than a synthetic dataset.
- Unit coverage added for valid/invalid StrategyPackage imports and language contract.

## Still requiring external integration or UI wiring

1. A real AI backend/API credential must be configured by the deployment environment; the app must never ship a secret in source.
2. Notification policy exists and is persisted, but Android notification-channel scheduling/background ingestion and the visible Settings controls still need final UI wiring.
3. StrategyPackage import is executable and validated, but the existing Import screen must invoke it and register the resulting strategy in the StrategyRegistry.
4. Local account verification is implemented, but production account/password/email changes require a real authenticated backend; local hashing must not be presented as server account management.
5. Language identifiers are complete, but all user-facing strings still need resource-level translations and runtime locale switching.
6. The build currently uses modern Kotlin compilerOptions; remaining third-party/runner deprecation notices must be verified on CI and eliminated where the project controls them.
7. APK size remains intentionally organic. No artificial padding is allowed. The 50 MB target is not a functional requirement and must not be met by useless bytes.

## Verification rule

Do not close the ALVEX milestone as 100% complete until the remaining external/UI integrations above have been implemented and verified with tests and a clean CI build. A green compile alone is not sufficient.
