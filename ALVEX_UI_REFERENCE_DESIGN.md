# ALVEX — UI/UX Reference Design (Normative)

**Status:** REQUIRED / SOURCE-OF-TRUTH DESIGN TARGET
**Product:** ALVEX — Professional Crypto Trading, Backtesting & Market Intelligence Platform

## 1. Purpose

This document permanently records the visual target agreed for ALVEX. The supplied CoinEx screenshots and the generated ALVEX multi-screen concept are references for information hierarchy, density, navigation and polish. ALVEX must have its own branding, icons and visual identity; it must not copy CoinEx branding or proprietary assets.

The generated concept is a **design target**, not a claim that the current APK already contains these screens. Implementation is complete only when the corresponding screen exists, is connected to real application behavior, and passes verification.

## 2. Global visual target

- Premium, world-class crypto-finance terminal quality.
- ALVEX branding everywhere; the old Notash product name must not appear in user-facing UI.
- Dedicated ALVEX logo and dedicated adaptive launcher icon.
- Professional dark theme as the flagship experience plus a complete light/system theme.
- Dense but readable financial information; no large empty prototype-like areas.
- Purpose-designed icons for every major section.
- Consistent typography, spacing, cards, borders, elevation, status colors and motion.
- Loading, skeleton, empty, offline, error and success states for every data-driven surface.
- Responsive layouts across Android phones and larger displays/densities.
- Touch-first interactions with accessible hit targets and readable text.

## 3. Required screen family

### 3.1 Login

A premium ALVEX entry screen with logo, authentication fields/actions, validation, loading/error states, session handling and language selection. No fake successful authentication.

### 3.2 Home / Dashboard

The primary screen follows the reference information hierarchy:

- Top-left: dedicated AI icon for AI Hub/contextual analysis.
- Top-right: dedicated Settings icon.
- ALVEX identity/search/notification access.
- Market overview cards.
- Major/common crypto prices and 24h movement.
- AI-selected Pump/Dump opportunities and risks.
- Recently pumped and recently dumped assets.
- Trending/New/Gainers/Losers/Volume/AI Picks/Meme/Pump-Dump/Whale discovery.
- Bottom navigation to the application's major functional areas.

Every market value must come from an actual provider or be explicitly marked unavailable.

### 3.3 Markets

Exchange-grade market list with search, favorites, filtering, sorting, price, 24h movement, volume, refresh state and timestamp. Selecting an asset opens its intelligence/detail view.

### 3.4 Trading / Chart

A professional chart screen with real candlesticks, timeframe controls, zoom/pan where supported, touch inspection, OHLC, volume where available, crosshair/tooltip, entry/exit markers, LONG/SHORT distinction, SL/TP and position lifecycle visualization.

### 3.5 AI Market Radar

A visual intelligence dashboard showing current alerts, signal severity, market context, timestamp, confidence and drill-down to the underlying evidence.

### 3.6 Whale Intelligence

A dedicated whale screen with Inflow/Outflow/Wallet categories, asset, amount/value when known, source, timestamp, direction, classification and confidence. HuntFlo is the intended Telegram intelligence source for whale/news activity. Pure price messages from HuntFlo are not required and must not be treated as whale intelligence.

### 3.7 Pump / Dump Radar

A ranked scanner with meaningful metrics: direction, score, confidence, price acceleration, volume anomaly, volatility, liquidity considerations and supporting signals. It must distinguish raw detection from AI interpretation.

### 3.8 Meme / Shitcoin Scanner

A high-risk asset discovery screen with filters, rankings, liquidity/volume/age metadata where available, holder concentration, tokenomics/unlock risks, security indicators where supported, pump/dump score, speculation score, risk level and data gaps.

### 3.9 Coin Intelligence

A coin detail workspace combining Market, Pump/Dump, Whale/Smart Money, Meme/Risk, Project Research, Team/Investors, Roadmap, Tokenomics, Unlock/Vesting/Burn/Emission and On-chain/Holder information. Scores must be explainable and timestamped.

### 3.10 Tokenomics

Professional allocation visualization, circulating/total/max supply, allocation categories, unlock/vesting relation, concentration and inflation/deflation indicators. The visualization must reflect actual data.

### 3.11 AI Backtest Analyst

A report workspace showing performance metrics and AI analysis. It must connect to actual backtest trades and explain profitable/losing trades, entry/exit behavior, SL/TP behavior, fees/funding and strategy improvement opportunities.

### 3.12 Settings

A professional settings center containing functional:

- Profile
- Security
- Notifications
- Privacy
- Support
- Share
- Language
- Theme
- Data Providers
- AI Provider

Support must use the configured support email rather than a fabricated address. Profile/security actions must use the actual authentication architecture.

## 4. Navigation rule

The application must not collapse all functionality into one screen. The target is a multi-section professional terminal. Recommended primary navigation:

1. Home
2. Markets
3. Terminal / Backtest
4. Intelligence
5. AI Hub
6. Portfolio / Reports

Settings remains accessible from the top-right gear.

## 5. Functional fidelity rule

The visual reference is binding for **quality and information architecture**, but not a permission to create decorative mockups. Every card, button, icon, chart control, scanner, report and list must either:

1. execute a real supported action;
2. display real data;
3. display a truthful provider/error/unavailable state.

No fake prices, fake whale events, fake AI findings, fake investors or fabricated project data.

## 6. AI behavior

AI surfaces must show:

- analysis/recommendation;
- evidence/signals used;
- confidence;
- timestamp/data freshness;
- source information where available;
- limitations/data gaps.

AI must never promise a guaranteed pump, dump or trading outcome.

## 7. Android quality target

- Compatible with supported Android versions and a broad range of phone screen sizes/densities.
- No hard-coded dimensions that break on other devices.
- Proper RTL support for Persian and Arabic while English remains the source language.
- Localized support for English, Persian, Arabic, French and Chinese.
- Accessible touch targets and content descriptions.
- Smooth scrolling and state restoration.
- Offline/error states where network data is required.

## 8. Definition of Done for each screen

A screen is considered complete only when:

- UI matches the ALVEX visual target;
- navigation works;
- every primary control has behavior;
- real data/provider integration is present where required;
- loading/error/empty/offline states are handled;
- localization is implemented;
- accessibility basics are covered;
- unit/integration tests exist for critical behavior;
- Android build succeeds;
- the relevant user flow has been manually or instrumentally verified.

## 9. Permanent product rule

**Do not remove existing ALVEX functionality to achieve this design.** Existing features are preserved and upgraded. Any refactor must improve architecture, reliability, usability or visual quality. The reference design is an evolution target for the existing application, not a reason to reset the project.
