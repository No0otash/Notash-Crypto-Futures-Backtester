# ALVEX — Global Product & UI/UX Work Order

This document is a permanent product requirement and must be used together with `PROJECT_TASKS_119.md`. It exists so the global product direction cannot be lost between ChatGPT/Codex sessions.

## Brand
- User-facing product name: **ALVEX**.
- Remove visible `NOTASH` branding from UI, launcher label and user-facing copy.
- Use a dedicated ALVEX launcher icon and consistent ALVEX visual identity.
- Package/namespace names may remain technically unchanged unless a future migration explicitly changes them; this does not count as visible branding.

## Screen Architecture
1. **Login** — premium authentication workspace with ALVEX logo, username/email, password, remember-me, password visibility and demo/local entry path. Do not claim backend authentication exists until a real auth provider is connected.
2. **Home / Market Command Center** — professional exchange-style dashboard. Physical left top position contains the AI icon. Physical right top position contains Settings. Center contains ALVEX identity/page title.
3. **Settings** — Profile, Security, Notifications, Privacy, Email Support and Share. Email support must open the device email composer using a configurable support address; do not hard-code or invent the user's private email.
4. **Market Pulse** — live CoinEx prices, 24h change, volume, search, quick markets and selectable symbols.
5. **AI Market Radar** — market signals and explainable pump/dump watchlist. Signals must be derived from market data/intelligence code, not decorative static cards.
6. **Pump/Dump & Intelligence** — real price/volume detector plus entry points for Meme/Shitcoin, Whale/Smart Money, Tokenomics/Unlock and Team/Investor intelligence.
7. **Backtest Terminal** — real CoinEx OHLC data, timeframe selection, backtest execution, metrics, trade-by-trade report and chart diagnostics.
8. **Curve / Candlestick** — real candlesticks, touch/click inspection, Long/Short trade markers, entry/exit prices and a connection between robot output and visual diagnostics.
9. **Strategy Lab** — editable strategy parameters and integration with the backtest flow; import paths must be connected to actual import implementation when available.
10. **AI Hub** — independent AI workspace consuming market, intelligence and backtest context. Provider-neutral architecture; no fabricated AI output or guaranteed predictions.

## Visual Quality Bar
- Professional financial-product hierarchy comparable in quality/density to major exchange apps, without copying their proprietary UI.
- Dark premium visual system, strong cards, clear hierarchy, compact market rows, meaningful icons, consistent spacing and typography.
- No page may be a wall of controls or a collection of decorative icons.
- Each major module must have a clear purpose, data state, loading state, error state and useful interaction.
- Use graphical icons for navigation and major features; avoid single-letter placeholder navigation.
- Persian UI must remain readable RTL while physical left/right requirements such as AI-left and Settings-right are preserved.

## Functional Quality Bar
- Live market sections use the existing CoinEx data layer.
- Pump/Dump uses the existing detector/intelligence layer.
- Backtest uses the existing BacktestViewModel/runner and report models.
- Candlestick chart uses actual OHLC data and supports touch inspection.
- Trade markers are tied to actual report timestamps/prices when a report exists.
- Equity Curve uses actual report equity data; no fake curve is presented as a backtest result.
- Settings actions must perform real local/UI behavior; external integrations must clearly identify missing credentials/configuration.
- No fabricated team, investor, tokenomics, on-chain, AI or market data.

## Verification Gate
Before a UI/product milestone is declared complete:
- `gradle testDebugUnitTest` must pass.
- `gradle assembleDebug` must pass.
- The APK must be generated and uploaded as an artifact by CI.
- A failed build is not a completed milestone, regardless of visual progress.

## Relationship to the 119-item work order
The 119-item registry remains the feature-level checklist. This document defines the global product shell, quality bar, branding and UX architecture that all 119 items must fit into.

---

# ALVEX UI/UX REFERENCE DESIGN — MANDATORY UPDATE

The visual concept supplied during the ALVEX project is now a permanent design target for the application. It is documented in `ALVEX_UI_REFERENCE_DESIGN.md` and must be used together with this work order and the 119-item registry.

## Mandatory visual target

The app must evolve toward a premium, world-class crypto-finance terminal with the same **level of information hierarchy, density, polish, navigation quality and functional richness** represented by the approved ALVEX concept. The concept is a target for ALVEX; it is not a claim that the current APK already implements every screen shown.

Required screen family:

1. **ALVEX Login** — dedicated logo/icon, professional authentication, validation and real session behavior.
2. **Home / Dashboard** — AI icon physically at the upper-left, Settings physically at the upper-right, market overview, AI-selected Pump/Dump candidates, recent pump/dump activity and professional discovery sections.
3. **Markets** — exchange-grade market list with search, favorites, sorting, filtering, prices, movement, volume and timestamps.
4. **Trading / Chart** — real candlesticks, timeframe controls, touch inspection, OHLC, volume where available, Long/Short markers, Entry/Exit, SL/TP and position lifecycle.
5. **AI Market Radar** — explainable alerts with severity, confidence, timestamp and drill-down evidence.
6. **Whale Intelligence** — Inflow/Outflow/Wallet views, source, timestamp, amount/value where known, classification and confidence. HuntFlo is the intended Telegram intelligence source for whale/news activity; price-only messages are not required as the whale source.
7. **Pump / Dump Radar** — ranked opportunities/risks based on measurable signals and AI interpretation, never guaranteed predictions.
8. **Meme / Shitcoin Scanner** — high-risk asset discovery, filters, liquidity/volume/age, holder concentration, tokenomics/unlock risk, security indicators where supported and meaningful risk scoring.
9. **Coin Intelligence** — unified Market, Pump/Dump, Whale, Meme/Risk, Project, Team/Investors, Roadmap, Tokenomics, Unlock/Vesting/Burn/Emission and On-chain information.
10. **Tokenomics** — actual supply/allocation/unlock information with meaningful visualizations.
11. **AI Backtest Analyst** — actual backtest data, trade-by-trade diagnosis, profit/loss causes, SL/TP behavior, fees/funding and strategy-improvement recommendations.
12. **Settings** — Profile, Security, Notifications, Privacy, Support, Share, Language, Theme, Data Providers and AI Provider, all functional.

## Visual and interaction rules

- ALVEX logo and dedicated adaptive app icon are mandatory.
- No old Notash branding in user-facing application UI.
- Premium dark theme plus complete light/system theme.
- Purpose-designed icons for major sections.
- Professional cards, compact financial rows, hierarchy, typography, spacing and motion.
- Loading, skeleton, empty, offline, error and success states must exist for data-driven screens.
- Responsive Android layouts; no device-specific hard-coded layout assumptions.
- Persian/Arabic RTL must work without breaking the physical AI-left / Settings-right requirement.
- English is the source language; Persian, Arabic, French and Chinese must be supported.
- No decorative-only controls.
- No fabricated market, whale, project, AI, investor, tokenomics or on-chain data.

## Functional definition of done

A screen is not complete merely because it resembles the reference. It is complete only when navigation, primary controls, data/provider integration, loading/error/empty/offline states, localization, accessibility basics and critical tests work, and the Android build succeeds.

## Preservation rule

This update does **not** authorize deletion of existing application functionality. Existing working features must be preserved and upgraded. Refactoring is allowed only when it improves reliability, architecture, usability, performance or visual quality.

**Canonical companion:** `ALVEX_UI_REFERENCE_DESIGN.md`
