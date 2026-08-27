# NOTASH Crypto Futures Backtester v2

این نسخه عمداً MetaTrader/MQL5 را حذف می‌کند و استراتژی را به یک موتور Crypto-native تبدیل می‌کند.

## معماری

Crypto Backtest Engine
- Exchange Adapter
  - CoinEx Futures (اولویت)
  - Binance Futures (آماده توسعه)
  - Bybit Futures (آماده توسعه)
  - Bitget Futures (آماده توسعه)
- Strategy Engine
- Risk Engine
- Funding Engine
- Fee/Slippage Engine
- Performance Engine
- Android Compose UI

## منطق استراتژی

- HTF LWMA 20/50
- LTF LWMA 20
- ATR 14
- Limit Entry = 0.5 ATR pullback
- SL = 1.5 ATR
- TP = 3 ATR
- Risk = درصدی از موجودی
- Leverage cap
- یک پوزیشن همزمان
- Funding-aware PnL
- Maker fee برای ورود Limit و Taker fee برای خروج (قابل تنظیم)

## CoinEx API

CoinEx API v2 برای Kline از:
GET /futures/kline

و برای Funding History از:
GET /futures/funding-rate-history

استفاده می‌شود. Kline تا 1000 رکورد در هر درخواست دارد و بازه‌های 1min، 3min، 5min، 15min، 30min، 1hour، 2hour، 4hour، 6hour، 12hour، 1day، 3day و 1week را پشتیبانی می‌کند.

Funding endpoint شامل actual_funding_rate و funding_time است.

مستندات رسمی:
https://docs.coinex.com/api/v2/

## نکته مهم

موتور بک‌تست داخل پروژه، منطق محاسباتی Crypto را دارد؛ Data Adapter باید برای دانلود pagination شده Kline و Funding و تبدیل آن به مدل Candle/FundingEvent تکمیل شود.

در برخورد همزمان SL و TP داخل یک کندل، سیاست محافظه‌کارانه SL-first استفاده شده تا نتیجه خوش‌بینانه نباشد.

## اجرا

پروژه را در Android Studio باز کنید:
1. Gradle Sync
2. Run روی گوشی یا Emulator
3. بعد از تکمیل Data Adapter، کلید RUN BACKTEST داده تاریخی را دریافت و بک‌تست را اجرا می‌کند.

## GitHub

کل پوشه را می‌توان در یک Repository قرار داد.

<!-- CI trigger after UI compile fixes -->
