# ALVEX Regression-Safe Navigation and Integration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Restore access to existing Trade Report/Strategy pages and wire the already-implemented Intelligence screen without rewriting or deleting existing feature code.

**Architecture:** Treat the current `feat/alvex-intelligence-ui` head as the only source of truth. Preserve existing screens and business logic; make only the smallest integration changes required in navigation/routing so existing reports remain reachable and the already-created Intelligence scanner is the screen shown for Intelligence. No replacement of new scanner code, no feature deletion, and no redesign of unrelated modules.

**Tech Stack:** Kotlin, Jetpack Compose Material 3, Android Gradle, JUnit, GitHub Actions.

**Spec:** `ALVEX_UI_REFERENCE_DESIGN.md` and `ALVEX_Master_Product_Spec_FA.docx`.

## Global Constraints

- Never base changes on an older APK or older commit; use the current PR head.
- Existing functionality is preserved; fixes are additive or narrowly corrective.
- Do not rewrite already-implemented Pump/Dump, Meme/Shitcoin, backtest, trade-report, or AI business logic.
- Intelligence must use the existing `AlvexIntelligenceScreen` implementation.
- Trade Report and Strategy pages must remain reachable from the app UI.
- `testDebugUnitTest` and `assembleDebug` must pass before completion.
- APK must be produced from the exact verified green commit.

---

### Task 1: Restore reachable existing pages and correct Intelligence routing

**Files:**
- Modify: `app/src/main/java/com/notash/cryptobacktester/ui/TerminalNavigation.kt`
- Modify: `app/src/main/java/com/notash/cryptobacktester/ui/ProfessionalTerminal.kt`
- Test: existing UI/navigation tests if present; add only a focused regression test if the current test structure supports it.

**Interfaces:**
- Preserve `TerminalPage.REPORT` and `TerminalPage.STRATEGY` exactly as existing page implementations.
- Preserve `AlvexIntelligenceScreen(fa, market)` exactly as implemented.
- Change only the navigation/routing references that currently make existing pages unreachable or bypass the new Intelligence screen.

- [ ] **Step 1: Verify current routing defect**
  - Confirm `ProfessionalTerminal` currently routes `TerminalPage.INTELLIGENCE` to `IntelligenceScreen` instead of `AlvexIntelligenceScreen`.
  - Confirm `TerminalPage.REPORT` and `TerminalPage.STRATEGY` exist but are absent from the primary navigation list.

- [ ] **Step 2: Apply the smallest routing fix**
  - Change only the Intelligence route to `AlvexIntelligenceScreen(fa, market)`.
  - Expose existing Report and Strategy destinations through the existing navigation mechanism without changing their screen implementations.
  - Do not modify `AlvexIntelligenceScreen.kt`.

- [ ] **Step 3: Verify compilation and focused tests**
  - Run `gradle testDebugUnitTest`.
  - Expected: all unit tests pass.

- [ ] **Step 4: Build the debug APK**
  - Run `gradle assembleDebug`.
  - Expected: successful APK generation.

- [ ] **Step 5: Commit the minimal fix**
  - Commit only the navigation/routing fix and the plan documentation.
  - Do not include unrelated formatting or feature changes.

---

### Task 2: Verify CI and regression safety

**Files:**
- No production-file changes unless CI exposes a concrete failure.
- Modify: `ALVEX_PROJECT_EXECUTION_LOG.md` only to record verified results.

- [ ] **Step 1: Wait for the GitHub Actions run for the new head**
- [ ] **Step 2: If red, inspect the exact failing test/build step and make the smallest corrective change on the latest head**
- [ ] **Step 3: Rerun CI until green**
- [ ] **Step 4: Confirm the final green run contains both unit tests and APK build**
- [ ] **Step 5: Extract the APK from that exact green run and verify its checksum**
- [ ] **Step 6: Record the final commit, workflow run, APK checksum, and preserved-feature rule in the execution log**

## Regression Checklist

- Home remains available.
- Markets remains available.
- Backtest/Terminal remains available.
- Trade-by-Trade Report remains available and is not deleted.
- Strategy remains available and is not deleted.
- AI Hub remains available.
- Intelligence opens `AlvexIntelligenceScreen`.
- Pump/Dump and Meme/Shitcoin existing scanner code is untouched.
- Existing chart/backtest/report logic is untouched.
- Settings remains accessible from the header.
