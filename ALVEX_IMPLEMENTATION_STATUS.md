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
- 🟢 Permanent local Demo Login is present on the Login screen. It creates a clearly separated local demo session without requiring Supabase, email confirmation, or real account credentials.

## Authentication / Demo Access — permanent product requirement

- 🟢 `Demo Login` must remain available on the Login screen as a local fallback for testing when Supabase authentication is unavailable or email confirmation is enabled.
- 🟢 Demo access must not modify, create, or impersonate a real Supabase account.
- 🟢 Demo identity is explicitly marked as demo/local and is independent from the real authenticated session.
- 🟢 Regression coverage exists in `app/src/test/java/com/notash/cryptobacktester/ui/DemoLoginTest.kt`.
- Any future Login/UI refactor must preserve this entry point unless the project owner explicitly approves its removal.

## Android Compatibility & Responsive UI — mandatory product requirement

- 🔴 REQUIRED: The entire ALVEX application must be designed and implemented for broad Android-device compatibility, not only Xiaomi 14 or one specific screen size.
- The UI must adapt to different screen widths/heights, aspect ratios, densities, orientations and Android device configurations without clipped, overlapping or inaccessible controls.
- Navigation, charts, tables, cards, dialogs, keyboards/input fields and touch targets must remain usable on small, medium and large Android screens.
- Android lifecycle/configuration changes must not break active screens, chart state or user-entered settings where state retention is expected.
- Avoid device-specific hard-coded dimensions, coordinates or assumptions. Prefer responsive Compose layouts, adaptive sizing and density-independent units.
- Compatibility verification must include at minimum the project's supported Android SDK range and representative small/medium/large screen configurations; Xiaomi 14/Android 16 remains a test device, not the only compatibility target.
- A green compile/build alone is not sufficient evidence of device compatibility. Compatibility requires automated tests where practical plus emulator/device rendering checks for critical screens.

## Professional Terminal — approved reference implementation

Reference target: premium dark crypto-finance terminal comparable in information hierarchy, density, polish and navigation to the supplied ALVEX reference image, while retaining ALVEX's independent visual identity.

- 🟢 ALVEX shell: branding, header, AI-left, Settings-right and page navigation are implemented.
- 🟢 Quick Actions and separate terminal pages are implemented.
- 🟢 Responsive shell contract is implemented with compact/standard/expanded breakpoints and a capped content width for larger screens.
- 🟢 Professional trading chart source is now committed in `ProfessionalTerminal.kt` rather than existing only as a workflow patch.
- 🟢 Chart renders real green/red OHLC candles, uses aggregated real market history, and preserves touch candle inspection.
- 🟢 Trade lifecycle visuals are implemented from real `TradeResult` data: LONG/SHORT marker, Entry, Exit, position path, SL and TP lines.
- 🟢 Trade-to-candle mapping uses actual Entry/Exit timestamps and prices through `TradingChartModel.kt`.
- 🟢 Equity Curve continues to use real backtest equity data.
- 🟡 Final physical-device visual QA remains open. The execution environment cannot certify rendering on a physical handset, so no handset-level completion claim is made.

## Verification evidence on 2026-09-12

- TDD responsive contract test exists in `app/src/test/java/com/notash/cryptobacktester/ui/TerminalResponsiveLayoutTest.kt` for compact, standard and expanded widths.
- TDD chart contract test exists in `app/src/test/java/com/notash/cryptobacktester/ui/TradingChartModelTest.kt`.
- TDD Demo Login contract test exists in `app/src/test/java/com/notash/cryptobacktester/ui/DemoLoginTest.kt`.
- The professional chart implementation was committed to `main` in commit `63ce47517f21b93256dc0ad9dc290a2ee237d1fc`.
- Demo Login implementation was added in commits `71577c5ac5d71c69bfcf9d2cd9d7591a713e5046`, `4c6006eb21c8ec68b6fb9e731879843febe0bf7d` and `059c160feb352aebf144949f2361e4e6877a9590`.
- A previous clean Android verification run is `33393559309` with unit tests, debug APK build and artifact upload passing; that run predates the final chart/responsive source commit and therefore is not reused as proof of the latest changes.
- The Demo Login change must be verified by the next clean GitHub Actions unit-test and APK build before being treated as CI-verified.

## Known non-complete production items

These must not be represented as finished merely because the APK builds:

1. Real AI provider credentials/backend are not included; fallback AI is intentionally non-fabricating.
2. HuntFlo public-channel ingestion is implemented, but notification scheduling/preferences and a complete persisted event feed still require UI/data-store integration.
3. Some existing Strategy/Import controls are still UI-only and require connection to the actual strategy importer.
4. Settings profile/security/password/email persistence is not yet a complete account backend.
5. Five-language localization is not yet a complete resource-level translation set.
6. The current build still emits Material icon and Gradle/Actions deprecation warnings; these are warnings, not build failures.
7. The APK is below the previously requested 50 MB visual target. Size must not be artificially inflated; size should grow only from useful functionality.
8. Physical-device rendering verification for the professional trading terminal/chart is still required.
9. Broad Android compatibility is now a mandatory requirement and is not considered complete until representative small/medium/large Android configurations and critical-screen rendering have been verified.

## Rule

Do not close the ALVEX milestone as 100% complete until the known non-complete items above have been implemented and verified with tests and a clean CI build.
