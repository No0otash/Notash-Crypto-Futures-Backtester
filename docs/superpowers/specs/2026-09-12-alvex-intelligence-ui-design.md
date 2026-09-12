# ALVEX Intelligence + Responsive UI Design

**Goal:** Restore and complete the real Pump/Dump and Meme/Shitcoin scanning flow while upgrading navigation and Android 16-safe responsive layout without removing existing ALVEX functionality.

## Scope

This change has two tightly related product tracks:
1. **Market Intelligence data flow:** expose the CoinEx futures market universe, collect current ticker/candle evidence for the universe, run the existing PumpDumpRadarEngine and MemeShitcoinScanner over real data, rank results, expose data gaps/timestamps, and feed the existing intelligence surfaces.
2. **ALVEX shell/UI:** replace fixed small-market Home assumptions with real scanner entry points, improve bottom navigation/icon identity and independent intelligence pages, and apply safeDrawing insets so Xiaomi 14/Android 16 content is not glued to the status bar.

Existing detector/scanner logic is retained and extended; no existing backtest, terminal, chart, auth, export, or report capability is removed.

## Architecture

`CoinExApi` gains a market-universe endpoint adapter and `CoinExRepository` exposes it through a provider-neutral market descriptor. A scanner service loads the universe, fetches ticker data for the configured scope with bounded concurrency, optionally fetches candles for qualifying candidates, and passes immutable evidence into the existing intelligence engines. The UI consumes a state model containing loading/success/empty/error/offline states and never substitutes fake market values when provider data is unavailable.

The Pump/Dump result distinguishes raw detector evidence from optional AI interpretation. Meme/Shitcoin results retain risk and data-gap information. The existing Coin Intelligence engine remains the drill-down destination for a selected asset.

## Pump/Dump behavior

Each scan evaluates available evidence for price change/acceleration, volume anomaly, volatility anomaly, divergence where candles support it, liquidity/spread when available, open-interest/funding context when available, and multi-timeframe confirmation when candle data is available. Scores and confidence are those produced by the existing engine; missing evidence is represented as a data gap rather than invented.

Results are grouped into Pump, Dump, Watch/Weak Signal, and high-risk/meme candidate views. Ranking is descending by score. Every row exposes symbol, direction, score, confidence, price/change, volume anomaly when available, timestamp, and concise evidence/reasons. A detail action opens Coin Intelligence/chart context.

## Meme/Shitcoin behavior

The existing `MemeShitcoinScanner` remains the scoring engine. The provider adapter supplies real market snapshots and candles where available. The UI supports search, sort, filtering and a ranked scanner view. Risk dimensions include volatility, liquidity, abnormal volume, concentration/project signals when the provider actually supplies them, and explicit Data Gaps. No unsupported contract/social/team claims are fabricated.

## UI/navigation

Use a professional terminal-inspired dark/light design system with compact information density, dedicated iconography for Home, Markets, Terminal, Intelligence, AI Hub and Portfolio/Reports. Settings stays accessible from the top-right gear. Intelligence provides independent entry points for Pump/Dump Radar and Meme/Shitcoin Scanner rather than placing everything on Home. Home shows a concise ranked opportunity preview and links to full scanners.

The top app shell uses Compose `WindowInsets.safeDrawing`/safe content handling and responsive layout primitives rather than a hard-coded centimeter offset. This specifically addresses Android 15/16 edge-to-edge behavior on devices such as Xiaomi 14 while preserving the same visual hierarchy across phone sizes.

## Error/offline handling

Scanner states: loading, success, empty-with-reason, provider error, timeout/rate-limit, and offline/stale-data. Cached/stale data must display its timestamp and stale status. Partial scans must identify the number of symbols scanned and any unavailable evidence rather than presenting a false complete-market claim.

## Testing

Unit tests cover market-universe parsing, scanner ranking, data-gap propagation, empty/partial provider responses, and the responsive top-inset contract. Existing intelligence and terminal tests remain green. CI must pass `testDebugUnitTest` and `assembleDebug`; the resulting debug APK is uploaded by the existing workflow.

## Non-goals

No rewrite of the existing PumpDumpDetector/PumpDumpRadarEngine scoring formula unless a failing test demonstrates a correctness defect. No deletion of existing features. No fake market data. No CoinEx branding/assets copied into ALVEX.
