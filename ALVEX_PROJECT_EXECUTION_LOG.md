# ALVEX — Project Execution Log & Non-Negotiable Rules

**Status:** PERMANENT PROJECT RECORD  
**Branch:** `feat/alvex-intelligence-ui`  
**Last verified implementation commit:** `e6c01c8ea90d5692d4942c3a766fed37abedb735`  
**Purpose:** Keep a durable record of work already performed, rules that must never be violated, and the workflow expected for every future change.

> This document supplements `ALVEX_MASTER_PRODUCT_SPEC.md` and `ALVEX_UI_REFERENCE_DESIGN.md`. Those documents remain the product and visual source of truth. This file records execution history and operational constraints.

## 1. Absolute working rules

1. **Always work from the latest version actually sent to the user.** Before changing code, verify the current branch/head commit and the latest successful APK/build associated with that version.
2. **Never modify an old version when a newer version exists.** Do not use an older commit, branch state, APK, or stale working copy as the base for new work.
3. **Do not delete existing functionality.** Improvements and fixes are additive unless the master specification explicitly requires replacing obsolete behavior. Existing working features must remain available.
4. **Do not silently redesign away requested functionality.** If a requested feature exists, preserve it and improve it rather than substituting a decorative mockup.
5. **No fake data.** Prices, whale events, AI findings, investors, project data, market signals and scanner results must come from real supported providers or be explicitly shown as unavailable/unknown.
6. **No decorative-only controls.** Every major button, icon, card, chart control and navigation item must perform a real supported action or expose a truthful unavailable/error state.
7. **Pump/Dump is a real scanner, not a static demo.** It must scan the available futures market universe, rank meaningful signals, expose evidence/confidence/timestamp/data gaps, and distinguish raw detection from AI interpretation.
8. **Meme/Shitcoin Scanner must be real and risk-aware.** It must use available market/project/on-chain data, expose data gaps, and never imply certainty about a pump or investment outcome.
9. **CoinEx is the primary crypto-futures target.** Do not reintroduce MetaTrader/MT5 as the app's core market-data target.
10. **Responsive Android UI is mandatory.** Do not use hard-coded dimensions that break on different phone sizes/densities. Respect system insets/edge-to-edge behavior and safe content areas.
11. **ALVEX branding is mandatory.** User-facing product identity must be ALVEX; repository ownership/history may retain the repository name.
12. **Run verification after each implementation step.** At minimum, relevant unit tests and `assembleDebug` must pass before calling a build complete.
13. **When a workflow fails, fix the actual failure and run the workflow again.** Continue until the relevant workflow is green. Do not hide or bypass failing tests.
14. **Do not claim an APK is ready until the corresponding latest build is verified green and its artifact is available.**
15. **Do not overcomplicate fixes.** Fix the root cause with the smallest safe change, then rerun verification.

## 2. Work completed / implemented to date

### 2.1 Product specification and design governance

- Added and maintained `ALVEX_MASTER_PRODUCT_SPEC.md` as the master functional/product specification.
- Added and maintained `ALVEX_UI_REFERENCE_DESIGN.md` as the normative visual/UX target.
- Established the premium exchange-inspired terminal direction while explicitly prohibiting copied CoinEx branding/proprietary assets.
- Established multi-section navigation rather than collapsing the application into one screen.
- Established requirements for loading, skeleton, empty, offline, error and success states.
- Established English source language plus Persian/Arabic/French/Chinese localization requirements and RTL support.

### 2.2 Authentication / demo access

- Added the local `Demo Login / ورود دمو` path so the app can be entered for demonstration/testing without depending on email confirmation.
- Kept demo access conceptually separate from the real authentication/session architecture.
- Added regression/documentation coverage for the demo-login behavior.
- A previously verified green build included this work (workflow #473).

### 2.3 Professional terminal / backtesting UI

- Professional terminal structure exists with real OHLC candlestick visualization rather than an equity-curve-only placeholder.
- Supported timeframe presentation includes 1m, 5m, 15m, 1h, 4h and 1d.
- Trade lifecycle visualization includes entry/exit markers, LONG/SHORT distinction and SL/TP information where the underlying trade data provides it.
- Equity Curve and metrics remain part of the backtesting experience.
- Responsive-layout tests were added for compact/standard/expanded layouts.
- Android system-inset handling was added to prevent content/header from being glued to the status-bar area.
- Home market pulse was moved toward the actual futures market universe instead of a fixed hard-coded symbol list.

### 2.4 CoinEx futures market integration

- Added futures market descriptors and ticker snapshots.
- Added CoinEx futures market-universe retrieval through `/futures/market`.
- Added all-futures ticker retrieval through `/futures/ticker`.
- Added market parsing and availability handling.
- Active/trading market filtering is represented in the intelligence scanning layer.

### 2.5 Pump/Dump intelligence

- Existing Pump/Dump detection engines were retained and integrated rather than replaced.
- Added a market-intelligence data-source abstraction.
- Added a CoinEx market-intelligence data source.
- Added a market-intelligence scanner that loads the futures universe and all available tickers, scans active markets, tracks unavailable data, and reports data gaps.
- Deep candle retrieval is limited to top candidates to avoid excessive per-market API requests.
- Intelligence UI includes Pump/Dump and Meme/Shitcoin areas with loading/error/empty/offline handling and scan summaries.
- Pump/Dump output is designed to expose score, confidence, reasons/signals and data gaps.

### 2.6 Meme/Shitcoin intelligence

- Added/integrated a market scanner using the same market-intelligence source.
- Missing liquidity is treated as missing data rather than falsely as zero.
- Risk/scanner output is designed to expose confidence and data gaps.

### 2.7 JVM-safe parser correction

- The CoinEx futures-market parser was changed from Android `org.json` parsing to `kotlinx.serialization` for the pure parsing function.
- Reason: local JVM unit tests can fail when Android framework `org.json` methods are invoked without an Android runtime.
- The network-facing Android API code was not unnecessarily rewritten; the change was isolated to the pure parser.
- Current verified implementation commit: `e6c01c8ea90d5692d4942c3a766fed37abedb735` (`Make CoinEx market parser JVM-safe`).

### 2.8 Testing / build verification

- Added/maintained tests covering futures-market parsing, market-intelligence scanning and meme-market scanning.
- A malformed JSON fixture in the parser test caused a workflow failure; it was corrected.
- A subsequent JVM runtime failure exposed the Android `org.json` local-test problem; the parser was then corrected as described above.
- Latest verified workflow at the time this document was written: **Build Android APK #505**, head `e6c01c8ea90d5692d4942c3a766fed37abedb735`, conclusion **success**.
- The successful workflow produced the `ALVEX-debug-apk` artifact.

## 3. Work that must NOT be done

The following are permanent prohibitions unless the master specification is deliberately changed and the change is documented:

- Do not reset/rebuild ALVEX from scratch merely to change the UI.
- Do not remove existing backtesting, charts, trade reports, exports, data/provider logic, intelligence modules or working authentication behavior to make a screen simpler.
- Do not replace real market data with static sample prices in production UI.
- Do not replace the Pump/Dump scanner with a few manually selected coins.
- Do not claim an AI signal is guaranteed to pump/dump or guarantee profit.
- Do not fabricate whale transactions, investor/team data, tokenomics, project facts or on-chain statistics.
- Do not turn unavailable provider data into misleading zeros.
- Do not make the entire product a single scrolling screen when the specification requires independent functional sections.
- Do not use huge empty prototype-style layouts where dense financial information is appropriate.
- Do not use CoinEx logos, proprietary assets or copied branding as ALVEX identity.
- Do not put Settings only in a bottom-navigation slot when the required top-right Settings entry is available; Settings belongs to the top-right gear flow.
- Do not hard-code phone-specific spacing/dimensions for Xiaomi 14 at the expense of other Android devices.
- Do not bypass, disable or delete failing tests just to make GitHub Actions green.
- Do not reuse an older APK/build as the basis for a new change when a newer user-facing version has already been produced.
- Do not report “complete”, “fixed”, “green” or “APK ready” without corresponding verification evidence.

## 4. Required workflow for future changes

1. Identify the latest user-facing APK/build and its exact commit.
2. Identify the current branch/head and verify that it matches that latest version.
3. Read the relevant section of `ALVEX_MASTER_PRODUCT_SPEC.md` and `ALVEX_UI_REFERENCE_DESIGN.md` before changing behavior/UI.
4. Make the smallest safe additive change.
5. Add or update regression tests for critical behavior.
6. Run GitHub Actions on the resulting latest commit.
7. If the workflow fails, inspect the actual failing step/log, fix the root cause on the **latest head**, and run the workflow again.
8. Repeat until the relevant workflow is green.
9. Verify the produced APK artifact belongs to the final green commit.
10. Report the exact commit/workflow/artifact to the user.

## 5. Current state snapshot

- Product: **ALVEX**
- Primary repo: `No0otash/Notash-Crypto-Futures-Backtester`
- Active intelligence/UI branch: `feat/alvex-intelligence-ui`
- Latest verified implementation commit recorded here: `e6c01c8ea90d5692d4942c3a766fed37abedb735`
- Latest verified Build Android APK workflow: **#505 — success**
- Latest successful artifact: **ALVEX-debug-apk**
- Master product spec: `ALVEX_MASTER_PRODUCT_SPEC.md`
- Normative UI target: `ALVEX_UI_REFERENCE_DESIGN.md`

## 6. Change-control rule

Any future assistant/agent working on ALVEX must treat this file, the master product specification and the normative UI reference as persistent project instructions. If a new requirement conflicts with an existing requirement, do not silently choose one: document the conflict and resolve it explicitly before removing or weakening existing functionality.
