# Hannah Backend

This backend is the trust boundary for the global product.

## Responsibilities

- Email OTP + CAPTCHA authentication
- User/account isolation and persistent strategy versions
- OpenAI Responses API access
- Structured AI strategy generation and validation
- AI trade/backtest diagnosis
- Rate limiting, audit logging and abuse protection
- Optional future exchange credential vault and deployment workers

## Security rule

The Android app must never contain `OPENAI_API_KEY`, SMTP credentials, CAPTCHA secrets, database admin credentials, or exchange API secrets. The mobile client calls this backend over HTTPS.

## AI strategy flow

1. Authenticated user submits natural-language strategy.
2. Backend validates request and rate limits the user.
3. Backend calls OpenAI Responses API with a strict strategy schema.
4. Backend validates the returned package against risk and syntax constraints.
5. Backend returns structured strategy JSON plus warnings and model/request IDs.
6. Android displays the rules and requires user approval before local backtesting.
7. Approved versions are stored under the authenticated user ID.

## Live trading

Do not enable autonomous live order placement in this first version. Deployment guidance is read-only/manual until a dedicated credential vault, exchange adapters, permissions policy, kill switch, position limits, idempotency and audit system are implemented.
