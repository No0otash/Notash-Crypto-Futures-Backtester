# Strategy / Trader Bot Import Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Let users import a strategy definition safely, inspect its parameters, use it in the existing backtester, and send the resulting report to the existing AI diagnosis flow.

**Architecture:** Add a small parser/import model layer that converts supported JSON/TXT/CSV strategy definitions into the existing `BacktestConfig` and a strategy metadata object. The UI uses Android's document picker, shows the parsed strategy, and passes the resulting configuration into the existing backtest flow without executing arbitrary imported code.

**Tech Stack:** Kotlin, Android Activity Result APIs, existing BacktestEngine/BacktestRunner, existing Compose/UI layer, JSON via org.json where already available.

**Spec:** Approved in chat on 2026-08-23.

## Global Constraints

- Preserve all existing professional terminal, AI analysis, exports, language switching, Top-10 selection, and menu layout functionality.
- Never execute arbitrary imported source code on-device.
- Unsupported or malformed files must produce a clear Persian error and leave the current strategy unchanged.
- Reuse the existing `BacktestConfig`, engine, and AI analysis rather than duplicating them.

---

### Task 1: Import domain and parser

**Files:**
- Create: `app/src/main/java/com/notash/cryptobacktester/imports/StrategyImport.kt`
- Modify only if needed: existing strategy/config model files

- [ ] Define imported strategy metadata and parsed configuration.
- [ ] Support JSON keys matching `BacktestConfig` and common aliases.
- [ ] Support simple key/value TXT.
- [ ] Support one-row CSV key/value input.
- [ ] Validate numeric ranges and preserve unspecified values from the current config.
- [ ] Return structured Persian-friendly parse errors.

### Task 2: Android document picker and import state

**Files:**
- Create: `app/src/main/java/com/notash/cryptobacktester/imports/StrategyImportController.kt`
- Modify: existing main/terminal UI only at the import entry point

- [ ] Register `ActivityResultContracts.OpenDocument` for JSON/TXT/CSV.
- [ ] Read the selected document as UTF-8.
- [ ] Parse without executing code.
- [ ] Keep current strategy when parsing fails.
- [ ] Expose imported strategy name, source filename, and parsed configuration.

### Task 3: Import UI

**Files:**
- Modify: existing `ProfessionalTerminal.kt` or its existing screen component

- [ ] Add visible `Import Strategy / Bot` action.
- [ ] Show selected filename and parsed parameters.
- [ ] Add `Use in Backtest` action.
- [ ] Add `Reset to Current Strategy` action.
- [ ] Add Persian/English strings using the existing language mechanism.
- [ ] Keep the existing layout and controls intact.

### Task 4: Backtest integration

**Files:**
- Modify: existing backtest state/runner integration only

- [ ] Pass imported `BacktestConfig` into the existing runner.
- [ ] Do not fork or replace the existing engine.
- [ ] Label reports with the imported strategy name.
- [ ] Feed the resulting report to `TradeAiAnalyzer`.

### Task 5: Verification

- [ ] Build debug APK.
- [ ] Verify malformed JSON/TXT/CSV does not crash the app.
- [ ] Verify imported values reach the existing backtest config.
- [ ] Verify existing AI analysis and CSV/JSON exports remain available.
- [ ] Verify Persian and English UI strings remain functional.
