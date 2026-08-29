# Global Product Implementation Plan

> **For agentic workers:** Implement this plan task-by-task. Preserve all existing capabilities; improve and connect them rather than replacing them.

**Goal:** Turn Notash Crypto Futures Backtester into a production-grade, globally scalable trading intelligence and backtesting platform.

**Architecture:** Preserve the current Android/Compose application while consolidating duplicated domain models behind stable core/data interfaces. Build real market-data, strategy, chart, backtest, AI, and token-intelligence capabilities incrementally, with each layer testable independently.

**Tech Stack:** Kotlin, Android, Jetpack Compose, Kotlin Serialization, Coroutines, CoinEx APIs, JUnit, GitHub Actions.

## Global Constraints

- Do not remove existing user-facing capabilities.
- Do not count mock/demo output as complete functionality.
- Strategy logic must be the source of truth for chart overlays and signals.
- Every milestone must compile and pass tests before the next milestone.
- Existing branch structure and working UI should be preserved unless a change is required for correctness.
- APK size is currently out of scope; prioritize functionality, stability and UX.

## UX Requirements Added From Reference Screenshots

### Main application navigation

- Remove the redundant/useless top header area highlighted in the reference screenshot; it consumes vertical space and provides no useful action.
- Keep one clear, intentional app header/navigation hierarchy instead of duplicated branding bars.
- The main navigation must remain compact, professional and usable on modern Android phones.
- Menu and Settings must be redesigned as polished, functional destinations rather than placeholder panels.
- Settings should expose useful application controls without clutter, with clear grouping, icons, descriptions and safe defaults.
- Preserve all existing navigation destinations and capabilities while improving their hierarchy.

### Login screen

- Login must closely follow the supplied CoinEx-style reference: clean light background, branded header area, large rounded form surface, account/email/phone field, password field with visibility toggle, validation messages, prominent login action, alternative Google login action and password-recovery entry.
- Support email/phone account input as shown in the reference.
- Preserve the language switch and make Persian/English behavior consistent with the rest of the app.
- Validation must be clear and localized; disabled login state must be visually obvious.
- Do not copy third-party branding/assets beyond what is necessary for the intended interaction pattern; use Hannah/Notash branding for the actual product.

## Milestones

- [ ] Stabilize domain models and eliminate duplicate definitions without changing behavior.
- [ ] Make market data and historical candles reliable and testable.
- [ ] Connect strategy indicators to the chart, including toggleable real MA/MMA overlays.
- [ ] Make strategy execution and futures backtesting deterministic and testable.
- [ ] Complete fees, funding, slippage, liquidation, risk and reporting calculations.
- [ ] Complete AI strategy generation and trade analysis with real inputs/outputs.
- [ ] Build token/project intelligence: roadmap, team, investors, utility, tokenomics, unlocks, vesting and burns.
- [ ] Build Growth Scanner, Pump/Dump detection, Whale/Smart Money and early-growth candidate ranking.
- [ ] Complete the independent AI Hub for education, Q&A, coin analysis and strategy/trade assistance.
- [ ] Implement the refined global navigation, Settings and login UX from the approved references.
- [ ] Harden security, localization, performance, export/share and production readiness.

## Preservation Requirements

- Keep the existing strategy manager and robot/strategy import capability.
- Existing backtest, chart, trade report, CoinEx and other exchange integrations must remain available.
- New intelligence features must extend existing models/services rather than silently replacing working functionality.

## Verification

After each code change: run `testDebugUnitTest`; only after green, run `assembleDebug`. Record the CI run and artifact before moving to the next milestone.
