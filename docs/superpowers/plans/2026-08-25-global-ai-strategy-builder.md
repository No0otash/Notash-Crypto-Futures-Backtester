# Global AI Strategy Builder Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Turn Hannah into a global, multilingual crypto-futures research terminal where users can describe a trading strategy in natural language, have AI convert it into a validated strategy package, backtest it, analyze the results, save it to their account, and provide exchange deployment guidance without exposing exchange or OpenAI secrets in the APK.

**Architecture:** Android remains the client and local backtest UI. A trusted backend owns authentication, CAPTCHA/OTP, OpenAI API access, strategy compilation/validation, user strategy persistence, audit/version history, and exchange credential operations. AI responses use strict structured JSON so generated strategies are machine-readable and can be validated before entering the backtest engine. Deployment starts as guided/manual exchange instructions; live execution is an explicit later capability with encrypted server-side credentials and per-order risk controls.

**Tech Stack:** Kotlin/Jetpack Compose Android, existing CoinEx backtest engine, HTTPS JSON backend, OpenAI Responses API with Structured Outputs, server-side secret management, per-user database.

**Spec:** This plan implements the user's global product requirements for email login + CAPTCHA, persistent user data, AI strategy generation from natural language, automatic backtest integration, AI diagnosis/improvement, and exchange deployment guidance.

## Global Constraints

- Preserve all existing Hannah/backtesting/chart/AI-analysis/import-strategy/top-10/market-radar capabilities.
- Never put OpenAI API keys, exchange API secrets, email-provider secrets, or server signing secrets in the Android APK.
- Generated strategies must be validated before execution and must not directly place live orders.
- User data is isolated by authenticated user ID and strategy versions are immutable/auditable.
- UI supports Persian and English and keeps one top-level menu.

---

### Task 1: AI strategy contract

**Files:**
- Create: `app/src/main/java/com/notash/cryptobacktester/ai/StrategyGenerationModels.kt`
- Create: `app/src/main/java/com/notash/cryptobacktester/ai/StrategyGenerationRepository.kt`
- Test: `app/src/test/java/com/notash/cryptobacktester/ai/StrategyGenerationModelsTest.kt`

- [ ] Define a strict strategy package containing name, version, market/timeframe, indicators, entry rules, exit rules, risk controls, filters, and human-readable explanation.
- [ ] Define generation request/result/error contracts.
- [ ] Validate required fields and reject unsafe/incomplete packages.
- [ ] Add unit tests for valid/invalid packages.

### Task 2: AI Strategy Builder UI

**Files:**
- Create/modify: `app/src/main/java/com/notash/cryptobacktester/ui/AiStrategyBuilderPage.kt`
- Modify: `app/src/main/java/com/notash/cryptobacktester/ui/HannahTerminal.kt`

- [ ] Add one menu item: `AI Strategy Builder` / `ساخت استراتژی با AI`.
- [ ] Let the user describe strategy in Persian or English.
- [ ] Collect symbol, timeframe, leverage, risk, and optional SL/TP constraints.
- [ ] Show generated strategy, rules, warnings, and confidence.
- [ ] Require user approval before importing into the backtest engine.
- [ ] Provide `Generate → Validate → Backtest → Analyze → Save Version` flow.

### Task 3: Backend AI gateway

**Files:**
- Create: `backend/README.md`
- Create: `backend/src/ai/strategy_generation_contract.json`
- Create: `backend/src/ai/strategy_generation_prompt.md`
- Create: `backend/src/auth/README.md`

- [ ] Define HTTPS endpoints for generation, validation, saving strategy versions, and retrieving user strategies.
- [ ] Call OpenAI Responses API only from the backend.
- [ ] Require structured output matching the strategy schema.
- [ ] Store model/version/request metadata for reproducibility.
- [ ] Rate-limit requests per authenticated user.

### Task 4: Persistence and account isolation

**Files:**
- Modify: `app/src/main/java/com/notash/cryptobacktester/auth/AuthRepository.kt`
- Create: `app/src/main/java/com/notash/cryptobacktester/auth/UserDataRepository.kt`
- Create: `backend/src/db/schema.sql`

- [ ] Persist user strategies, versions, backtest summaries, AI reports, preferences, and import history by user ID.
- [ ] Preserve existing local strategy storage as an offline cache.
- [ ] Sync after authentication and resolve conflicts by version.

### Task 5: Exchange deployment assistant

**Files:**
- Create: `app/src/main/java/com/notash/cryptobacktester/ui/DeploymentGuidePage.kt`
- Create: `app/src/main/java/com/notash/cryptobacktester/exchange/ExchangeDeploymentGuide.kt`

- [ ] Generate exchange-specific setup checklists for CoinEx, Binance, Bybit, Bitget and extensible adapters.
- [ ] Explain API key permissions, IP restrictions, testnet/demo where supported, leverage/risk settings, bot upload/configuration, and verification steps.
- [ ] Keep deployment manual/read-only until a dedicated live-trading security layer is implemented.

### Task 6: Verification

- [ ] Run unit tests.
- [ ] Run `./gradlew testDebugUnitTest`.
- [ ] Run `./gradlew assembleDebug`.
- [ ] Verify no existing features are removed.
- [ ] Verify secrets are absent from source/APK configuration.
