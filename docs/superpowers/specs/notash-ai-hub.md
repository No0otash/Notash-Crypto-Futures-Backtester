# NØTASH AI HUB

NØTASH AI HUB is an independent first-class product area inside the app. It must not replace or weaken the existing Backtester, Trading Terminal, Strategy Manager, Market Radar, or Growth Scanner.

## Modes

1. **Chat AI** — open-ended questions about crypto, trading, futures, indicators, risk, tokenomics, on-chain data, strategies, and general learning.
2. **Learn** — interactive education from beginner to advanced, with adaptive explanations, examples, quizzes, and progress tracking.
3. **Trade Analyst** — analyze selected backtests and live trade records, including entry/exit, SL/TP, PnL, fees, funding, drawdown, and exit reason.
4. **Strategy Analyst** — inspect an imported/created bot strategy and explain its rules, indicators, weaknesses, overfitting risk, and optimization opportunities.
5. **Coin Research** — generate a structured research dossier for any supported token, including low-volume and meme/shit coins, while clearly exposing missing/unverified data.
6. **Market Research** — combine BTC trend, dominance, sector strength, volume, liquidity, OI, funding, long/short positioning, momentum, relative strength, news and narrative.

## Safety / trust requirements

- AI must distinguish verified facts, calculated metrics, and hypotheses.
- Missing data must never be converted into positive evidence.
- Low-confidence token research must show `Data Confidence: Low` and `High Risk` where appropriate.
- AI must not claim a token will pump or guarantee profit.
- Research output must preserve source timestamps and stale-data state when available.
- Recommendations must explain the evidence behind the conclusion.

## Architecture

- Independent UI route/screen and state container.
- Independent AI service/repository boundary.
- Context adapters for Backtester, Strategy Manager, Market Radar, Token Intelligence, and live market data.
- Conversation history and user-controlled context inclusion.
- Persian/English support.
- Graceful offline/fallback mode with explicit limitations.
- Provider abstraction so the AI backend can change without rewriting the app.

## Acceptance criteria

A user can enter AI HUB independently, ask a free-form question, receive a contextual answer, switch learning/research modes, and optionally attach app context. Existing app capabilities remain available exactly as before.
