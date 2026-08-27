# Global Product v4 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Evolve the existing Crypto Futures Backtester into a production-grade global crypto trading terminal, strategy platform, market radar, token intelligence engine, and AI growth scanner without removing existing user-facing capabilities.

**Architecture:** Preserve the existing backtest/strategy/data layers and add bounded services for live market data, token intelligence, on-chain analytics, radar scoring, AI analysis, alerts, and a professional Compose UI. Existing Strategy import/add/edit/delete/export flows remain first-class and become the source of truth for chart indicators and backtests.

**Tech Stack:** Kotlin, Android Compose, Gradle, CoinEx API adapters, local persistence, serialization, deterministic scoring/calculation services, and testable repository interfaces.

**Spec:** User-approved 118-item product scope in conversation.

## Global Constraints

- Do not remove existing user-facing capabilities; improve or migrate them.
- No fake/mock feature may be presented as production-ready functionality.
- Missing token data lowers Data Confidence and never creates a positive assumption.
- Growth candidates must emphasize pre-move signals, not only assets that already pumped.
- AI outputs are probabilistic scenarios and risk analysis, not guaranteed returns.
- Backtest calculations remain deterministic and testable.
- Every new subsystem requires automated tests before final integration.
- Existing Strategy import/add/edit/delete/export functionality must remain available.

---

### Workstreams

1. Core/backtest hardening and regression coverage.
2. Strategy lifecycle and indicator/chart integration.
3. Professional chart and trading terminal.
4. Live market and exchange adapters.
5. Pump/dump, whale, news, and market radar.
6. Per-token intelligence and tokenomics/unlock/on-chain analysis.
7. AI Growth Scanner, scoring, scenarios, confidence, and alerts.
8. Global UX, localization, security, performance, release validation.

### Final validation

Run unit, integration, serialization, data, strategy, chart, scoring, and Android packaging checks; verify that legacy feature flows remain reachable; produce a release APK only after all mandatory checks pass.
