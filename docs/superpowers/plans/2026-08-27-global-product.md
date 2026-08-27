# Global Product Implementation Plan

> **For agentic workers:** Implement this plan task-by-task. Preserve all existing capabilities; improve and connect them rather than replacing them.

**Goal:** Turn Notash Crypto Futures Backtester into a production-grade, globally scalable trading intelligence and backtesting platform.

**Architecture:** Preserve the current Android/Compose application while consolidating duplicated domain models behind stable core/data interfaces. Build real market-data, strategy, chart, backtest, AI, and token-intelligence capabilities incrementally, with each layer testable independently.

**Tech Stack:** Kotlin, Android, Jetpack Compose, Kotlin Serialization, Coroutines, CoinEx APIs, JUnit, GitHub Actions.

**Spec:** Product requirements agreed in conversation.

## Global Constraints

- Do not remove existing user-facing capabilities.
- Do not count mock/demo output as complete functionality.
- Strategy logic must be the source of truth for chart overlays and signals.
- Every milestone must compile and pass tests before the next milestone.
- Existing branch structure and working UI should be preserved unless a change is required for correctness.

## Milestones

- [ ] Stabilize domain models and eliminate duplicate definitions without changing behavior.
- [ ] Make market data and historical candles reliable and testable.
- [ ] Connect strategy indicators to the chart, including toggleable real MA/MMA overlays.
- [ ] Make strategy execution and futures backtesting deterministic and testable.
- [ ] Complete fees, funding, slippage, liquidation, risk and reporting calculations.
- [ ] Complete AI strategy generation and trade analysis with real inputs/outputs.
- [ ] Build token/project intelligence: roadmap, team, investors, utility, tokenomics, unlocks, vesting and burns.
- [ ] Harden security, localization, performance, export/share and production readiness.

## Current first task

Consolidate the strategy layer carefully. `core.Models` is the canonical model source for candles/trades; `data.StrategyPackage` is the serializable strategy package. Avoid introducing another `StrategyPackage` in `engine`. Preserve any public API needed by existing callers through adapters rather than deleting functionality.

### Verification

After each code change: run `testDebugUnitTest`; only after green, run `assembleDebug`. Record the CI run and artifact before moving to the next milestone.
