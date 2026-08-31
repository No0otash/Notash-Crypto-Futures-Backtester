# ALVEX — Master Product & Engineering Specification

**Status:** Living master reference / source of truth
**Product:** ALVEX — Professional Crypto Trading, Backtesting & Market Intelligence Platform
**Repository:** No0otash/Notash-Crypto-Futures-Backtester
**Audience:** Product, UX/UI, Android engineering, data engineering, AI engineering, QA

> This document consolidates the user's explicit requirements, the previously agreed 119-item work order, and the design/architecture recommendations made during the project. It is the persistent reference for future Codex/ChatGPT sessions. Do not treat a feature as complete merely because its screen, icon, button, or card exists: a feature is complete only when its underlying behavior is implemented, connected to real or clearly identified data, handles failure states, and is verified.

---

## 0. Non-negotiable product principles

1. **The product name is ALVEX.** Remove the visible product branding/name "Notash" from the application UI, app label, launcher identity, user-facing copy, and product-facing documentation where it represents the app brand. Repository ownership/history may remain unchanged.
2. **ALVEX must look and behave like a world-class financial application**, not a prototype. Visual quality must be comparable in polish, hierarchy, density, responsiveness, navigation, charts, states, and interaction quality to leading crypto trading products.
3. **No decorative-only UI.** Every major icon, card, menu, chart control, scanner, intelligence result, report, and action must have a real function or a clearly marked unavailable/coming-soon state.
4. **Do not duplicate existing functionality.** Before implementing any item, inspect the repository and map existing implementations to this specification and `PROJECT_TASKS_119.md`.
5. **Preserve working functionality.** Existing backtesting, strategies, charting, metrics, pump/dump, whale/smart-money, meme/shitcoin, and intelligence code must be reused and improved rather than replaced with parallel fake implementations.
6. **No fabricated data.** If a provider does not supply a field, show `Unknown`, `Unavailable`, or an explicit data gap. Never invent prices, investors, holders, AI findings, or project facts.
7. **AI must be explainable.** AI recommendations must show the signals/data used, confidence, timestamp and relevant sources where available. Never claim certainty or guaranteed price movement.
8. **Provider-neutral architecture.** Market-data, on-chain and AI providers must be behind interfaces so providers can be changed without rewriting domain/UI code.
9. **Real verification is mandatory.** A release candidate is not complete until unit tests and the Android debug build succeed. Critical user flows must also be exercised.
10. **Mobile-first, Android-first, production architecture.** The UI must work on the user's Android device class and adapt to different screen sizes, densities and orientations where supported.

---

# 1. Product identity and visual language

## 1.1 Brand

- Product name: **ALVEX**.
- Dedicated ALVEX logo.
- Dedicated launcher/app icon.
- Adaptive Android icon where applicable.
- Splash/launch identity consistent with ALVEX.
- No generic placeholder avatar/iconography where a product-specific visual is expected.
- Do not use the old Notash product branding in user-facing screens.

## 1.2 Visual direction

Target a premium crypto-finance terminal rather than a simple CRUD application:

- Professional dark theme as a first-class experience.
- High-quality light theme as a first-class experience.
- Strong typography hierarchy.
- Dense but readable financial information.
- Cards and panels with clear grouping.
- Consistent spacing, corner radius, iconography and elevation.
- Purpose-designed icons for every major navigation area.
- Subtle transitions, loading states, skeletons, empty states, error states and success feedback.
- Responsive layouts for phones and larger screens.
- Avoid huge empty areas and avoid placing all functionality into one long screen.
- Avoid generic black circles with arbitrary glyphs when a purpose-designed visual is appropriate.

## 1.3 Navigation

Use a professional multi-section structure. Suggested primary navigation:

1. **Home**
2. **Markets**
3. **Terminal / Backtest**
4. **Intelligence**
5. **AI Hub**
6. **Portfolio / Reports**

Settings is opened from the top-right gear and is not required to consume a primary bottom-navigation slot.

---

# 2. Login / authentication

## 2.1 Login screen

Required:

- ALVEX logo and brand.
- Professional login/register presentation.
- Email/credential entry as supported by the chosen authentication architecture.
- Validation and useful error messages.
- Loading state.
- Secure handling of credentials/tokens.
- Session state and logout.
- No fake login success.

If authentication is not backed by a production identity provider yet, the screen must make the development/demo state explicit rather than pretending to provide secure cloud authentication.

---

# 3. Home / Market landing screen

The Home screen must be visually inspired by the information hierarchy of professional exchanges such as the supplied CoinEx reference images, without copying their branding or proprietary UI.

## 3.1 Header

- **Top-left:** dedicated AI icon/button opening AI Hub or contextual AI analysis.
- **Top-right:** dedicated Settings icon/button.
- ALVEX branding where appropriate.
- Search access.
- Optional notification indicator.

## 3.2 Market overview

A prominent market area containing:

- Major crypto prices.
- 24h percentage movement.
- 24h volume where available.
- Market trend indicators.
- Search and filtering.
- Favorites/watchlist.
- Refresh state and last-updated timestamp.

Initial priority assets may include BTC, ETH and other liquid assets, but the system must not be hard-coded to only ten coins.

## 3.3 AI-selected Pump/Dump candidates

A dedicated section where ALVEX AI/intelligence ranks coins that show evidence of unusual upside or downside risk.

Each candidate should expose:

- Symbol/name.
- Current price and 24h change.
- Volume/volume anomaly.
- Pump/Dump score.
- Main signals that caused the score.
- Confidence.
- Timestamp.
- Risk label.
- Link to Coin Intelligence details.
- Link to chart.

The system must distinguish **AI recommendation** from raw market signal and must never state that a pump/dump is guaranteed.

## 3.4 Recently pumped / dumped

Separate lists for:

- Recent strong upward moves.
- Recent strong downward moves.
- Detection timestamp.
- Magnitude.
- Volume anomaly.
- Relevant intelligence signals.

## 3.5 Market tabs / discovery

Provide exchange-style discovery categories such as:

- Trending
- New
- Top Gainers
- Top Losers
- Highest Volume
- AI Picks
- Meme Scanner
- Pump/Dump
- Whale Activity

The exact labels can be localized.

---

# 4. Settings

Settings opens from the top-right icon and contains real navigable sections:

1. **Profile**
   - Display name and account information supported by authentication.
   - Preferences.
2. **Security**
   - Session/logout controls.
   - Credential/security settings supported by the authentication provider.
   - Secure API-key storage policy.
3. **Notifications**
   - Enable/disable categories.
   - Pump/dump alerts.
   - Whale alerts.
   - AI alerts.
   - System notifications.
4. **Privacy**
   - Data collection/privacy controls appropriate to the app.
   - Clear explanation of locally stored data.
5. **Support**
   - Contact/support action connected to the owner's configured support email.
   - The real email address must be configured from a secure project configuration; never invent an email address.
6. **Share**
   - Android share action for supported reports/content.
7. **Language**
   - Persian and English architecture.
8. **Theme**
   - Dark / Light / System.
9. **Data Providers**
   - Market and intelligence provider configuration/status.
10. **AI Provider**
   - Provider-neutral status/configuration.

Every settings item must navigate to a functional screen or action; no dead-end rows.

---

# 5. Markets

Professional market list:

- Coin icon.
- Symbol/name.
- Price.
- 24h change.
- Volume.
- Optional market cap.
- Watchlist/favorite.
- Sort/filter.
- Search.
- Pull/refresh or equivalent refresh action.
- Loading/error/empty states.
- Last update timestamp.

Selecting a coin opens its Coin Intelligence / market detail page.

---

# 6. Trading / Backtest Terminal

This is the core analytical workspace.

## 6.1 Inputs

- Coin/pair.
- Historical/live mode.
- Timeframe.
- Initial balance.
- Trade amount.
- Leverage.
- Strategy.
- Fees.
- Funding configuration.
- SL/TP settings where strategy supports them.

## 6.2 Chart

The chart must be a functional trading chart, not an illustration.

Required:

- Real candlesticks.
- Timeframe controls.
- OHLC information.
- Zoom/pan where technically appropriate.
- Touch interaction.
- Crosshair or equivalent point inspection.
- Volume where data exists.
- Entry markers.
- Exit markers.
- Long/Short distinction.
- SL marker/line.
- TP marker/line.
- Position lifecycle visualization.
- Tooltip/data panel when a candle or trade marker is touched.

## 6.3 Robot trade diagnostics

A primary purpose is to find errors in the user's trading robot.

For every trade, show where it opened and closed on the chart and expose:

- Trade ID.
- Side: LONG/SHORT.
- Entry timestamp/price.
- Exit timestamp/price.
- Timeframe.
- SL.
- TP.
- Exit reason.
- Whether SL was touched.
- Whether TP was touched.
- PnL.
- PnL percentage.
- Fees.
- Funding.
- Duration.
- Strategy signal/reason if available.

Selecting a trade on the chart must connect to its detailed record.

## 6.4 Equity Curve

The Equity Curve must use actual backtest results and support:

- Interactive touch/inspection.
- Balance/equity at selected point.
- Trade/event correlation.
- Drawdown visualization.
- Useful axis labels.
- Reset/zoom where supported.

The curve must never be a static placeholder image.

## 6.5 Metrics

At minimum:

- Initial balance.
- Final balance.
- Net PnL.
- ROI.
- Win rate.
- Loss rate where available.
- Profit factor.
- Max drawdown.
- Total trades.
- Winning trades.
- Losing trades.
- Fees.
- Funding.
- Average win/loss where calculable.

---

# 7. Strategy Manager

- Strategy list.
- Create/edit/delete where appropriate.
- Strategy parameters.
- Active strategy.
- Validation.
- Backtest execution.
- Strategy result comparison.
- Preserve imported robot strategies.
- Avoid silently executing malformed strategies.

---

# 8. Robot Import

- Import supported robot/strategy format.
- Parse and validate.
- Show validation errors.
- Preserve strategy parameters.
- Map supported signals to the backtester.
- Clearly report unsupported logic.
- Do not claim full fidelity if a robot language/feature cannot be represented.

---

# 9. Export / reports

Support:

- CSV export.
- JSON export.
- AI analysis report.
- Share through Android share sheet.
- Trade-level report.
- Summary metrics.
- Data/source timestamps.
- Clear file naming using ALVEX branding.

---

# 10. Live market / CoinEx integration

The application must have a clean provider interface for live market data.

Requirements:

- CoinEx-ready data adapter.
- Real price retrieval where credentials/endpoints permit.
- Clear connection state.
- Timeout/error handling.
- Retry policy.
- Timestamp of last successful data.
- No fake live values when disconnected.
- Separation between live and historical data.

---

# 11. Pump / Dump Detector

Functional detector using measurable signals where data is available:

- Price acceleration.
- Percentage move.
- Volume spike.
- Volume/price divergence.
- Volatility anomaly.
- Liquidity considerations where available.
- Multi-timeframe confirmation.
- Score.
- Confidence.
- Direction.
- Detection time.
- Explanation.

Outputs must feed Home, Coin Intelligence and AI Hub.

---

# 12. Whale / Smart Money

- Whale transaction/activity ingestion through provider-neutral adapters.
- Smart-money signal abstraction.
- Buy/sell direction where known.
- Asset.
- Amount/value where available.
- Timestamp.
- Wallet classification where reliably known.
- Confidence.
- Source.
- Aggregation into a risk/signal score.

Unknown wallet identity must remain unknown.

---

# 13. Meme / Shitcoin Scanner (89–94)

This section must be a real intelligence feature, not a list with decorative badges.

## 13.1 Discovery

- Detect/filter meme and high-risk speculative assets using available market/project metadata.
- Symbol/name/chain/contract where available.
- Age/listing information where available.
- Liquidity/volume indicators where available.

## 13.2 Risk analysis

Evaluate available signals such as:

- Extreme volatility.
- Low liquidity.
- Abnormal volume.
- Concentrated holders.
- Large unlocks.
- Anonymous/opaque team.
- Weak/no product utility.
- Contract/security flags where a provider supports them.
- Rapid social/market activity where reliable data exists.

## 13.3 Scoring

Expose:

- Meme score.
- Speculation score.
- Rug/risk indicators where data exists.
- Pump/dump score.
- Whale concentration score.
- Overall risk.
- Confidence.
- Data gaps.

## 13.4 UI

- Dedicated scanner page.
- Filters.
- Sort.
- Search.
- Coin cards/list rows with meaningful metrics.
- Detail drill-down into Coin Intelligence.

---

# 14. Coin Intelligence Engine (95–104)

The engine is the central aggregator for coin-level research.

It should combine:

- Market data.
- Pump/Dump signals.
- Whale/Smart Money.
- Meme/Shitcoin analysis.
- Project/Product research.
- Team/Founder/Investors.
- Roadmap.
- Tokenomics.
- Unlock/Vesting/Burn/Emission.
- On-chain/Holder concentration.

Output:

- Overall score.
- Risk score.
- Market score.
- Fundamental/project score.
- Tokenomics score.
- On-chain score.
- Smart-money score.
- Meme/speculation score.
- Confidence.
- Data freshness.
- Explainable findings.
- Sources.
- Data gaps.

---

# 15. Project / Product Research (105–108)

Capture and analyze:

- Project name/symbol/category/chain.
- Contract address.
- Website/whitepaper/GitHub/explorer/social sources where available.
- Problem/solution.
- Product and utility.
- Token necessity/use cases.
- Product maturity.
- Mainnet/testnet/live status.
- Development activity.
- Repository/release activity where available.
- Strengths/weaknesses.
- Research summary.
- Source/timestamp/confidence.

---

# 16. Team / Founder / Investors (109–111)

- Founders/team and roles where reliably sourced.
- Previous experience where sourced.
- Transparency/anonymity state.
- Investors/backers.
- Funding rounds.
- Lead investor and other participants where known.
- Team score.
- Investor quality/funding strength.
- Risk flags.
- Confidence/source classification: Confirmed / Reported / Unknown.

---

# 17. Roadmap / Milestones (112–113)

- Roadmap availability.
- Milestones.
- Target dates.
- Completed/in-progress/upcoming/delayed/unknown.
- Completion rate.
- Delay rate.
- Missed milestones.
- Credibility/progress score.
- Sources and timestamps.

---

# 18. Tokenomics (114–116)

Track:

- Circulating supply.
- Total supply.
- Max supply.
- Initial supply.
- Fully diluted supply/value where applicable.
- Allocation by category.
- Team/advisors.
- Investors.
- Community/ecosystem.
- Treasury/foundation.
- Marketing/liquidity/staking/airdrop.
- Unlock/vesting relation.
- Supply concentration.
- Inflation/deflation.
- Validation of allocation totals.

Produce tokenomics score and risk explanation.

---

# 19. Unlock / Vesting / Burn / Emission (117)

Track:

- Next unlock.
- Future unlocks.
- Amount/percentage.
- Category.
- Cliff.
- Vesting period.
- Burn events/mechanism.
- Emission rate/schedule.
- Inflation impact.
- Dilution/supply-pressure score.
- Burn offset where calculable.
- Sources/timestamps/confidence.

---

# 20. On-chain / Holder Concentration (118)

Track where provider support exists:

- Holder count.
- Top 10/20/50/100 concentration.
- Largest wallets.
- Exchange wallets.
- Burn wallets.
- Treasury/foundation wallets.
- Team wallets.
- Smart-money wallets.
- Holder growth trend.
- Distribution score.
- Concentration risk.

Known exchange/burn/treasury wallets must not be blindly treated as ordinary holders.

---

# 21. Independent AI Hub (119)

AI Hub is an independent product area, not merely an AI button that opens a static screen.

## 21.1 Capabilities

- General AI chat.
- Crypto education.
- Coin analysis.
- Strategy analysis.
- Trade-by-trade analysis.
- Risk analysis.
- Research mode.
- Report generation.
- Explainable recommendations.
- Import/use exported backtest data.
- Share AI reports.

## 21.2 Coin analysis input

AI may consume:

- Market data.
- Pump/Dump signals.
- Whale/Smart Money.
- Meme scanner.
- Coin Intelligence.
- Project research.
- Team/investors.
- Roadmap.
- Tokenomics.
- Unlocks.
- On-chain data.

## 21.3 Strategy analysis

Analyze:

- ROI.
- PnL.
- Win rate.
- Drawdown.
- Profit factor.
- Fees/funding.
- Trade distribution.
- Entry/exit behavior.
- Long/Short balance.
- SL/TP behavior.
- Exit reasons.
- Repeated loss patterns.
- Potential strategy weaknesses.

## 21.4 AI safety/quality

- Provider-neutral interface.
- Real provider status.
- Explicit fallback mode.
- No fake AI output presented as provider output.
- Confidence/data gaps.
- Sources when research data is used.
- No guaranteed predictions.

---

# 22. Cross-feature data architecture

Preferred conceptual layers:

**UI → ViewModel/Presentation → Domain Use Cases → Repository Interfaces → Provider/Data Sources**

Core domain areas should be independently testable:

- Backtesting.
- Market data.
- Pump/Dump.
- Whale/Smart Money.
- Meme scanner.
- Coin Intelligence.
- Research.
- Team/Investors.
- Roadmap.
- Tokenomics.
- Unlocks.
- On-chain.
- AI.
- Export/reporting.

Do not let UI composables/screens directly own provider/network logic.

---

# 23. Data quality and provenance

Every external intelligence result should carry, where applicable:

- `source`
- `timestamp`
- `confidence`
- `status`
- `dataFreshness`
- `dataGaps`

Recommended status values:

- CONFIRMED
- REPORTED
- ESTIMATED
- UNKNOWN
- UNAVAILABLE

Never silently convert unavailable information into a plausible-looking value.

---

# 24. Error handling and resilience

Every network/provider-dependent feature must have:

- Loading state.
- Success state.
- Empty state.
- Offline state.
- Error state.
- Retry action where appropriate.
- Last-known timestamp where safe.
- No crash on malformed provider data.

Parsing must be defensive, especially for strategy imports and external JSON.

---

# 25. Accessibility and localization

- Persian RTL support.
- English LTR support.
- Correct number formatting.
- Correct financial decimal precision.
- Accessible touch targets.
- Content descriptions for meaningful icons.
- Text must not be embedded into images for dynamic UI.
- Layout must survive longer Persian strings.

---

# 26. Performance

- Avoid unnecessary recompositions/reloads.
- Cache safe market metadata.
- Paginate or virtualize long lists.
- Keep chart rendering responsive.
- Do not block the UI thread with backtests or parsing.
- Handle large trade histories.
- Use background execution for expensive analysis.

---

# 27. Security

- Never hard-code private API keys or secrets.
- Never commit production credentials.
- Secure local storage for user/API secrets.
- Do not log credentials.
- Validate external input.
- Clearly distinguish read-only market credentials from trading credentials.

---

# 28. Quality gates

A feature is **Complete** only if:

1. The implementation exists.
2. It is reachable from the correct UI.
3. Its primary action actually works.
4. It uses real repository/domain state rather than hard-coded demo data.
5. Loading/empty/error states exist where applicable.
6. It does not break existing functionality.
7. Relevant tests pass.
8. `gradle testDebugUnitTest` succeeds.
9. `gradle assembleDebug` succeeds.
10. The generated APK is inspectable and the artifact is uploaded by CI when CI is used.

Build warnings should be triaged. A warning is not automatically a build failure, but deprecated APIs should be migrated when practical, especially before a Gradle/Kotlin upgrade makes them errors.

---

# 29. Definition of Done for the ALVEX product

The project should not be declared "finished" merely because CI is green.

The release candidate is ready only when:

- ALVEX branding is complete.
- Login works according to the selected auth implementation.
- Home is a functional market dashboard.
- Settings sections are functional.
- Market data is connected through the configured provider.
- Pump/Dump detection is functional with explainable signals.
- Meme/Shitcoin Scanner is functional.
- Whale/Smart Money is functional where provider data exists.
- Coin Intelligence aggregates all available intelligence.
- Backtest terminal works with real results.
- Candlestick chart is interactive.
- Entry/exit/SL/TP markers are correctly mapped to trades.
- Equity Curve is interactive and connected to backtest output.
- Strategy Manager and Robot Import behave honestly about supported functionality.
- CSV/JSON/AI reports export correctly.
- AI Hub is independently navigable and provider-neutral.
- Research/team/roadmap/tokenomics/unlock/on-chain modules handle unavailable data correctly.
- Persian/English and dark/light experiences are coherent.
- Major icons are ALVEX-specific and meaningful.
- Critical user flows have been manually/automatically verified.
- CI tests and debug APK build are green.

---

# 30. Existing 119-item mapping

This document supersedes the ambiguity of the old high-level list while preserving its numbering:

- 1–15 Backtester Core
- 16–25 Chart / Candlestick
- 26–31 Entry / Exit / Long / Short
- 32–38 Equity Curve / Metrics
- 39–44 Fees / Funding / PnL
- 45–53 Strategy Manager
- 54–58 Robot Import
- 59–65 Settings / Timeframe / Leverage
- 66–70 Export CSV / JSON / AI Report
- 71–76 Live Market / Coin Prices
- 77–82 Pump / Dump Detector
- 83–88 Whale / Smart Money
- 89–94 Meme / Shitcoin Scanner
- 95–104 Coin Intelligence Engine
- 105–108 Project / Product Research
- 109–111 Team / Founder / Investors
- 112–113 Roadmap / Milestones
- 114–116 Tokenomics
- 117 Unlock / Vesting / Burn / Emission
- 118 On-chain / Holder Concentration
- 119 Independent AI Hub

For the detailed requirements behind the numbered items, use this document first and `PROJECT_TASKS_119.md` as the historical task registry.

---

# 31. Change-control rule

Future requests should update this document rather than relying on chat history alone.

When a requirement changes:

1. Update this document.
2. Record the change in the commit message.
3. Re-check affected existing functionality.
4. Do not delete an old requirement unless the new requirement explicitly supersedes it.
5. Mark implementation status separately from specification status.

**Chat history is not the source of truth. This file is.**
