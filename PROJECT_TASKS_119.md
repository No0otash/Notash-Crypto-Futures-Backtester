# Notash Crypto Futures Backtester — 119-Item Project Task Registry

This file is the persistent project work-order reference for Codex and future development sessions. Existing implementations must be preserved; duplicate work must not be recreated. Items should only be marked complete after implementation and successful verification.

## 1–14 Existing Product Areas

1–15 — Backtester Core: preserve and complete the core backtesting engine, trade lifecycle, balances, execution and extensibility.
16–25 — Chart / Candlestick: candlestick chart, timeframes, visual market data and trading markers.
26–31 — Entry / Exit / Long / Short: entry/exit markers, side, SL/TP and position visualization.
32–38 — Equity Curve / Metrics: equity curve, ROI, PnL, drawdown, win rate, profit factor and related metrics.
39–44 — Fees / Funding / PnL: trading fees, funding, PnL calculations and reporting.
45–53 — Strategy Manager: strategy management, selection, configuration and preservation of existing strategies.
54–58 — Robot Import: preserve and complete robot/strategy import capability.
59–65 — Settings / Timeframe / Leverage: configurable amount, leverage, timeframe and app settings.
66–70 — Export: CSV, JSON and AI-report export/share pipeline.
71–76 — Live Market / Coin Prices: real market data and coin-price connectivity architecture.
77–82 — Pump / Dump Detector: pump/dump detection and risk signals.
83–88 — Whale / Smart Money: whale activity, smart-money analysis and risk signals.
89–94 — Meme / Shitcoin Scanner: meme/shitcoin classification, risk signals, scoring and integration with intelligence.
95–104 — Coin Intelligence Engine: aggregate market/intelligence signals into a unified coin assessment.

## 15 — Project / Product Research (105–108)

105. Project Overview — Create a standard project profile containing name, symbol, chain/network, contract address, category, project type, description, objective, problem/solution, website, whitepaper, official social links, GitHub, explorer, project status, source and timestamp. Unknown data must remain Unknown; never fabricate data.

106. Product / Utility Analysis — Analyze product, real-world use case, target users, utility, token necessity, token use cases, product maturity, mainnet/testnet status, live-vs-idea status, strengths, weaknesses, product risks, score and confidence.

107. Project Activity / Development — Analyze repository/release/development activity where reliable data exists: latest release, latest commit, commit trend, contributors, issues/PR activity, release history, last activity, development score and risk. Missing data must be explicit.

108. Research Summary — Combine 105–107 into an integrated research result containing project summary, product summary, development summary, strengths, weaknesses, risks, data gaps, overall score, confidence, sources and timestamp. Output must be consumable by Coin Intelligence Engine.

## 16 — Team / Founder / Investors (109–111)

109. Team / Founder Intelligence — Identify founder/co-founders/core team and roles only from reliable sources; include official profiles where available, previous experience where sourced, anonymity status, transparency score, team risk, confidence and sources.

110. Investors / Backers — Identify investors, VCs/funds, strategic investors, funding rounds, round type, amount/date when available, lead investor, other backers, source and confidence. Distinguish Confirmed, Reported and Unknown.

111. Team / Investor Assessment — Produce team score, transparency score, investor quality, funding strength, reputation signals, risk flags, anonymous-team flag, unknown-data flag, overall assessment and confidence.

## 17 — Roadmap / Milestones (112–113)

112. Roadmap Engine — Capture roadmap milestones with title, description, target date, status (completed/in progress/upcoming/delayed/unknown), source and last update. Identify whether roadmap is available and whether it is official or third-party.

113. Milestone Analysis — Calculate completion rate, delay rate, upcoming/missed milestones, development progress, roadmap credibility, progress score and risk flags. Output must feed Coin Intelligence.

## 18 — Tokenomics (114–116)

114. Supply Structure — Track circulating, total, max, initial and fully diluted supply when available; inflationary/deflationary status, supply change, circulating percentage and remaining supply, with source/timestamp. Detect low circulation, large future unlock, unlimited/unknown supply conditions.

115. Token Allocation — Track allocation to team, advisors, investors, private/public sale, community, ecosystem, treasury, marketing, liquidity, staking, airdrop, foundation and other categories; include percentage, amount, unlock/vesting and source/confidence. Validate totals.

116. Tokenomics Assessment — Analyze supply, allocation, investor/team allocation, inflation, dilution, concentration, utility and related risks; produce tokenomics score, risk score, confidence and explainable findings.

## 19 — Unlock / Vesting / Burn / Emission (117)

117. Token Unlock Engine — Track unlock date/amount/percentage/category, next and future unlocks, cliff, vesting start/end and schedule, burn amount/rate/events/mechanism, emission rate/schedule and inflation. Calculate supply pressure, dilution, unlock, emission and burn-offset risks and an overall unlock score.

## 20 — On-chain / Holder Concentration (118)

118. On-chain Intelligence — Analyze holder count, top 10/20/50/100 holders, concentration, largest holders, exchange/burn/treasury/team wallets when identifiable, smart-money wallets when data exists, distribution, whale concentration and holder-growth trend. Calculate concentration/distribution risk and scores. Exchange, burn and treasury wallets must not be blindly counted as ordinary holders.

## 21 — Independent AI Hub (119)

119. Independent AI Hub — Build an independent AI workspace separate from Backtester while allowing it to consume Backtester and Intelligence data. It must support:
- General AI chat and crypto education.
- Coin analysis using market, pump/dump, whale, meme, coin-intelligence, project research, team/investor, roadmap, tokenomics, unlock and on-chain outputs.
- Strategy analysis using ROI, PnL, win rate, drawdown, profit factor, fees, funding, trades, entry/exit, side and SL/TP.
- Per-trade analysis and explainable reasons for success/failure.
- Risk analysis across market, liquidity, tokenomics, unlock, holder, whale, team, product and strategy risk.
- Education modes suitable for simple, intermediate and advanced explanations.
- Research mode with sources and timestamps.
- AI report generation and integration with CSV/JSON/report export.
- Provider-neutral architecture allowing OpenAI, Gemini, local or future providers without coupling the domain layer to one provider.
- Explicit fallback behavior when no real AI provider is connected; never present fabricated analysis as real data.
- Explainable outputs, confidence and data gaps.
- No claims of guaranteed price prediction or certainty.

## Implementation Rules

- Before changing code, inspect the current repository and map existing functionality to these items.
- Do not duplicate functionality already implemented in items 1–119.
- Preserve existing Backtester, Strategy Manager, Robot Import, Chart, Intelligence, Pump/Dump, Whale/Smart Money and Meme/Intelligence functionality.
- Reuse existing domain models and architecture where appropriate instead of creating parallel competing models.
- Prefer provider-neutral interfaces for external data and AI.
- Never fabricate missing market, team, investor, tokenomics or on-chain data; expose Unknown/data gaps with source and timestamp.
- New functionality must compile with the current Kotlin/Gradle setup and integrate with existing UI architecture.
- Do not add redundant tests solely to inflate test counts; use existing tests and add tests only where required to verify new behavior or prevent regressions.
- Before declaring work complete, run `gradle testDebugUnitTest` and `gradle assembleDebug` and require both to succeed.

## Status Convention

🟢 Complete = implemented and verified by successful build/tests.
🟡 Partial = some implementation exists but one or more requirements remain.
🔴 Missing = no reliable implementation found.

The registry itself is not proof of completion. Status must be based on the actual repository code and successful verification.