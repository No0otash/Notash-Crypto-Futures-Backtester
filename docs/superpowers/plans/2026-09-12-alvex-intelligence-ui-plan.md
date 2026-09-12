# ALVEX Intelligence + Responsive UI Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Restore real all-market Pump/Dump and Meme/Shitcoin scanning, connect it to ALVEX intelligence screens, and make the Android shell responsive and Android-16-safe without deleting existing features.

**Architecture:** Add a CoinEx market-universe adapter behind `CoinExRepository`, then build a bounded scanner service that feeds existing intelligence engines with real ticker/candle evidence and explicit data gaps. Upgrade the Compose shell/navigation and intelligence pages to consume scanner state and safe system insets while retaining the existing terminal/backtest/auth/report flows.

**Tech Stack:** Kotlin, Jetpack Compose, existing OkHttp/JSON CoinEx client, JUnit, Gradle Android build.

**Spec:** `docs/superpowers/specs/2026-09-12-alvex-intelligence-ui-design.md`

## Global Constraints

- Preserve all existing ALVEX functionality; only additive fixes/improvements.
- No fake market values or unsupported intelligence claims.
- Pump/Dump must scan the provider market universe rather than a hard-coded six-symbol list.
- Missing provider evidence must become explicit Data Gaps.
- Settings remains reachable from the top-right gear.
- Use responsive Compose sizing and `WindowInsets.safeDrawing`; do not hard-code a physical centimeter offset.
- CI must pass `testDebugUnitTest` and `assembleDebug`.

---

### Task 1: Add a real CoinEx futures market universe

**Files:**
- Modify: `app/src/main/java/com/notash/cryptobacktester/data/CoinExApi.kt`
- Modify: `app/src/main/java/com/notash/cryptobacktester/data/CoinExRepository.kt`
- Create: `app/src/test/java/com/notash/cryptobacktester/data/CoinExMarketParsingTest.kt`

**Interfaces:**
- Produce `data class FuturesMarketDescriptor(val market: String, val baseAsset: String, val quoteAsset: String, val isTrading: Boolean)` in the data layer.
- Produce `fun getFuturesMarkets(): List<FuturesMarketDescriptor>` on `CoinExApi` and `suspend fun loadFuturesMarkets(): List<FuturesMarketDescriptor>` on `CoinExRepository`.

- [ ] **Step 1: Write the failing parser test**

Test a representative CoinEx `/futures/market` JSON response containing two trading markets and one inactive market. Assert symbol/base/quote/status parsing and that inactive entries are retained with `isTrading=false`.

- [ ] **Step 2: Run the focused test and verify failure**

Run `gradle testDebugUnitTest --tests com.notash.cryptobacktester.data.CoinExMarketParsingTest`; expected failure because the parser/API method does not yet exist.

- [ ] **Step 3: Implement the endpoint adapter**

Add a GET request to `$BASE_URL/futures/market`, validate `code == 0`, parse `data`, and map common `market`, `base_ccy`, `quote_ccy`, and `status` fields with safe fallbacks. Throw a descriptive exception for non-success HTTP/API responses.

- [ ] **Step 4: Add the repository suspend wrapper**

Delegate to `CoinExApi` on `Dispatchers.IO`, matching the existing repository pattern.

- [ ] **Step 5: Run the focused test**

Run the same test and require PASS.

- [ ] **Step 6: Commit**

Commit message: `feat: expose CoinEx futures market universe`.

---

### Task 2: Build bounded all-market Pump/Dump scan orchestration

**Files:**
- Create: `app/src/main/java/com/notash/cryptobacktester/intelligence/MarketIntelligenceScanner.kt`
- Create: `app/src/test/java/com/notash/cryptobacktester/intelligence/MarketIntelligenceScannerTest.kt`
- Modify: `app/src/main/java/com/notash/cryptobacktester/intelligence/PumpDumpRadarOrchestrator.kt` only if needed to accept scanner evidence without changing existing callers.

**Interfaces:**
- `data class ScannerConfig(val maxMarkets: Int = 250, val candlePeriod: String = "5min", val candleLimit: Int = 60, val concurrency: Int = 6)`.
- `data class MarketScanReport(val signals: List<RadarPumpDumpSignal>, val scannedMarkets: Int, val unavailableMarkets: Int, val scannedAtMs: Long, val errors: List<String>)`.
- `suspend fun scanPumpDump(config: ScannerConfig = ScannerConfig()): MarketScanReport`.

- [ ] **Step 1: Write failing tests for ranking and partial failure**

Create fake market/ticker/candle loaders. Assert that a strong positive market ranks above weak signals, inactive markets are skipped, a failed candle request creates a data gap instead of deleting the ticker signal, and `scannedMarkets`/`unavailableMarkets` are accurate.

- [ ] **Step 2: Run the focused tests and verify failure**

Run `gradle testDebugUnitTest --tests com.notash.cryptobacktester.intelligence.MarketIntelligenceScannerTest`; expected failure because the scanner service does not exist.

- [ ] **Step 3: Implement the scanner**

Load the futures universe, keep trading markets, cap work with `maxMarkets`, fetch ticker snapshots, derive previous-volume/volatility evidence from recent candles when available, invoke the existing `PumpDumpRadarEngine` through `PumpDumpRadarOrchestrator`, and preserve explicit data gaps for unavailable evidence. Use bounded coroutine concurrency; do not issue an unbounded request per market.

- [ ] **Step 4: Add truthful partial/error states**

Return a report even when some markets fail. If the universe call fails, surface an error state to the caller rather than returning a fabricated empty success. If zero markets are returned, return an empty report with a reason.

- [ ] **Step 5: Run the focused tests and existing intelligence tests**

Run the scanner test plus `PumpDumpRadarEngineTest` and require PASS.

- [ ] **Step 6: Commit**

Commit message: `feat: scan CoinEx futures for pump dump signals`.

---

### Task 3: Wire Meme/Shitcoin scanning to real market candidates

**Files:**
- Create: `app/src/main/java/com/notash/cryptobacktester/intelligence/MemeMarketScanner.kt`
- Create: `app/src/test/java/com/notash/cryptobacktester/intelligence/MemeMarketScannerTest.kt`
- Modify: `app/src/main/java/com/notash/cryptobacktester/intelligence/MemeShitcoinScanner.kt` only for missing-field/data-gap propagation required by tests.

**Interfaces:**
- `data class MemeMarketScanReport(val results: List<MemeScanResult>, val scannedMarkets: Int, val unavailableMarkets: Int, val scannedAtMs: Long, val errors: List<String>)`.
- `suspend fun scanMemeMarkets(config: ScannerConfig = ScannerConfig()): MemeMarketScanReport`.

- [ ] **Step 1: Write failing tests**

Assert that only candidates with meaningful meme/speculation evidence are ranked as such, missing liquidity/market-cap/candle evidence is retained in Data Gaps, and provider failures do not become risk scores.

- [ ] **Step 2: Run the focused test and verify failure**

Run `gradle testDebugUnitTest --tests com.notash.cryptobacktester.intelligence.MemeMarketScannerTest`; expected failure.

- [ ] **Step 3: Implement candidate construction**

Build `MemeCoinSnapshot` from available provider fields and candle-derived volatility/volume features. Reuse `MemeShitcoinScanner`; do not invent social, contract, holder, team, or tokenomics information absent from the provider.

- [ ] **Step 4: Run meme scanner and existing Coin Intelligence tests**

Require `MemeMarketScannerTest` and `CoinIntelligenceEngineTest` to pass.

- [ ] **Step 5: Commit**

Commit message: `feat: connect meme scanner to market data`.

---

### Task 4: Replace fixed Home intelligence data with scanner state

**Files:**
- Modify: `app/src/main/java/com/notash/cryptobacktester/ui/ProfessionalTerminal.kt`
- Create: `app/src/main/java/com/notash/cryptobacktester/ui/IntelligenceState.kt`
- Create: `app/src/test/java/com/notash/cryptobacktester/ui/IntelligenceStateTest.kt`

**Interfaces:**
- `sealed interface IntelligenceState` with `Loading`, `Success`, `Empty`, `Error`, and `Offline` states.
- Success carries ranked pump/dump and meme results plus `scannedAtMs`, scanned count and unavailable count.

- [ ] **Step 1: Write failing state tests**

Assert that partial scans render as success-with-data-gap metadata, zero-result scans are `Empty`, and provider failures are `Error` rather than silently displaying the old fixed BTC/ETH/SOL/XRP/DOGE/PEPE list.

- [ ] **Step 2: Run focused tests and verify failure**

Run `gradle testDebugUnitTest --tests com.notash.cryptobacktester.ui.IntelligenceStateTest`; expected failure.

- [ ] **Step 3: Implement state mapping and Home integration**

Replace the hard-coded six-symbol intelligence source with the scanner state. Keep Home concise: top Pump/Dump opportunities, recently abnormal candidates, and entry points to full Intelligence scanners. Preserve existing backtest navigation.

- [ ] **Step 4: Add real click targets**

Pump/Dump cards open the dedicated scanner/detail context; Meme cards open Meme/Shitcoin scanner; selected coins route into existing Coin Intelligence/chart entry points.

- [ ] **Step 5: Run state and UI-adjacent unit tests**

Require PASS for the focused tests and existing terminal tests.

- [ ] **Step 6: Commit**

Commit message: `feat: wire ALVEX home to live intelligence scans`.

---

### Task 5: Upgrade independent Intelligence screens and navigation icons

**Files:**
- Modify: `app/src/main/java/com/notash/cryptobacktester/ui/IntelligenceScreen.kt`
- Modify: relevant navigation/root Compose file discovered from the current main activity during implementation.
- Create or modify: focused UI model/icon tests where the project already has navigation tests.

**Interfaces:**
- Independent routes for `Pump/Dump Radar`, `Meme/Shitcoin Scanner`, `Coin Intelligence`, and existing AI/Whale intelligence entries.
- Each route consumes the shared scanner state and has loading/error/empty/offline presentations.

- [ ] **Step 1: Add failing navigation-contract tests where feasible**

Assert every major bottom-nav item has a stable destination and Intelligence has separate Pump/Dump and Meme/Shitcoin destinations; Settings remains top-right.

- [ ] **Step 2: Run the focused navigation tests and verify failure**

Run the applicable existing navigation test class; if no navigation test framework exists, create a pure route-contract test instead.

- [ ] **Step 3: Implement professional icon identity**

Use distinct Material/Compose icons or existing project vector assets for Home, Markets, Terminal, Intelligence, AI Hub, Portfolio/Reports. Remove ambiguous decorative icons and dead buttons. Keep labels compact and readable in Persian/English.

- [ ] **Step 4: Implement independent scanner layouts**

Pump/Dump: ranked tabs/filter chips, direction, score, confidence, evidence, timestamp, data gaps, detail action. Meme/Shitcoin: Search, Sort, Filter, ranked rows/cards, risk dimensions, confidence, data gaps, Coin Intelligence action.

- [ ] **Step 5: Verify navigation and compile**

Run `gradle testDebugUnitTest` and `gradle assembleDebug`; require both PASS.

- [ ] **Step 6: Commit**

Commit message: `feat: upgrade ALVEX intelligence navigation and scanner UI`.

---

### Task 6: Fix Android 16 edge-to-edge and responsive shell spacing

**Files:**
- Modify: the root Compose/activity file containing the ALVEX top app bar and scaffold, discovered from the current repository.
- Modify: `app/src/main/java/com/notash/cryptobacktester/ui/ProfessionalTerminal.kt` if its top shell owns the relevant padding.
- Create/modify: `app/src/test/java/com/notash/cryptobacktester/ui/TerminalResponsiveLayoutTest.kt` only if the existing contract needs expansion.

**Interfaces:**
- Root content must apply `WindowInsets.safeDrawing` (or an equivalent `safeContent` strategy already used by the project) to top/bottom interactive content.
- Top menu/gear must have responsive safe-area spacing without a hard-coded physical-unit offset.

- [ ] **Step 1: Write/extend failing responsive tests**

Assert compact, standard and expanded layouts keep the top action row below the safe drawing area and do not rely on fixed pixel/centimeter offsets.

- [ ] **Step 2: Run focused responsive tests and verify failure**

Run `gradle testDebugUnitTest --tests com.notash.cryptobacktester.ui.TerminalResponsiveLayoutTest`; expected failure if the contract is not yet satisfied.

- [ ] **Step 3: Apply safe insets**

Use `Modifier.windowInsetsPadding(WindowInsets.safeDrawing)` or the project’s equivalent Compose inset API at the shell level, then use responsive content padding for visual breathing room. Do not add a literal `1.cm` or device-specific status-bar height.

- [ ] **Step 4: Preserve terminal/chart behavior**

Do not change candle rendering, trade markers, Entry/Exit, SL/TP, Long/Short, timeframes, or equity curve behavior except for safe layout placement.

- [ ] **Step 5: Run responsive and full unit tests**

Require PASS.

- [ ] **Step 6: Commit**

Commit message: `fix: make ALVEX shell safe for Android 16 edge to edge`.

---

### Task 7: Final verification and APK artifact

**Files:**
- Modify only files required by failing verification.
- No feature deletions.

- [ ] **Step 1: Run full unit test suite**

Run `gradle testDebugUnitTest`. Expected: PASS.

- [ ] **Step 2: Build debug APK**

Run `gradle assembleDebug`. Expected: PASS and `app/build/outputs/apk/debug/app-debug.apk` exists.

- [ ] **Step 3: Trigger/inspect GitHub Actions**

Push/commit changes to `main` through the repository workflow and inspect the resulting Actions run. Require the existing `testDebugUnitTest`, `assembleDebug`, APK inspection, and artifact upload steps to be green.

- [ ] **Step 4: Verify the artifact exists**

Use the workflow artifact named `ALVEX-debug-apk`; record run ID, artifact ID, APK byte size and SHA-256 when available.

- [ ] **Step 5: Final source audit**

Search for the old fixed six-symbol Home list and verify it is no longer the source of Pump/Dump intelligence. Search for literal centimeter/device-specific top offsets and remove any introduced by this work.

- [ ] **Step 6: Commit final verification/docs if needed**

Commit message: `chore: verify ALVEX intelligence and responsive shell` only if verification changes source/docs; otherwise do not create a no-op commit.
