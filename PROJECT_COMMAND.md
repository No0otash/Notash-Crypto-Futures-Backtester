# دستور پروژه — Alvex

> **مرجع الزامی توسعه پروژه**
>
> این سند مرجع اصلی و پایدار برای تصمیم‌گیری، توسعه، اصلاح، تست و تغییرات پروژه **Alvex / Notash Crypto Futures Backtester** است.

## 0. قانون اصلی و غیرقابل‌مذاکره

1. **قبل از هر عملیات روی پروژه** — شامل تغییر کد، UI/UX، معماری، dependency، تست، Build، CI/CD، مستندسازی فنی یا رفع خطا — ابتدا باید همین فایل (`PROJECT_COMMAND.md`) از شاخه `main` در GitHub مطالعه و الزامات مربوط به عملیات مشخص شود.
2. پس از هر تغییر واقعی در قابلیت‌ها، معماری، وضعیت تکمیل، محدودیت‌ها یا الزامات پروژه، **همین سند باید در همان تغییر/بلافاصله پس از آن به‌روزرسانی شود**.
3. هیچ قابلیت موجودی بدون دلیل و بررسی حذف، جایگزین یا دوباره‌سازی نشود.
4. قبل از پیاده‌سازی هر بخش، وضعیت فعلی Repository بررسی و با این سند تطبیق داده شود؛ قابلیت موجود باید حفظ و تکمیل شود، نه اینکه نسخه موازی و تکراری ساخته شود.
5. این سند از فایل‌های قدیمی‌تر Work Order مرجع بالاتری دارد. `PROJECT_TASKS_119.md` آرشیو/رجیستری ۱۱۹ آیتمی پروژه است و نباید با این سند در تضاد باشد.
6. وضعیت «Complete» فقط زمانی مجاز است که قابلیت واقعاً در کد وجود داشته و Build/Test موردنیاز با موفقیت انجام شده باشد. وجود صرفِ مدل، فایل، TODO یا متن مستندات، اثبات تکمیل نیست.

---

# 1. هویت و هدف محصول

**نام محصول:** Alvex

**هدف:** یک اپلیکیشن حرفه‌ای Android برای بک‌تست استراتژی‌های Crypto Futures، تحلیل بازار و پروژه‌های رمزارزی، مدیریت استراتژی، گزارش معاملات و تحلیل هوش مصنوعی.

محصول باید ظاهر حرفه‌ای و مدرن در سطح اپ‌های معاملاتی شناخته‌شده داشته باشد، اما هویت بصری مستقل Alvex را حفظ کند.

---

# 2. قابلیت‌های اصلی Backtester و Trading Terminal

## 2.1 موتور بک‌تست

- اجرای بک‌تست استراتژی.
- چرخه کامل معامله و مدیریت موجودی.
- Entry / Exit.
- LONG / SHORT.
- Stop Loss / Take Profit.
- کارمزد معاملات.
- Funding.
- PnL و درصد PnL.
- ROI.
- Win Rate.
- Drawdown.
- Profit Factor.
- معماری قابل توسعه برای اتصال Bot/Strategy.

## 2.2 نمودار بازار

- Candlestick chart واقعی با کندل‌های سبز/قرمز.
- Timeframeهای مختلف.
- نمایش Entry.
- نمایش Exit.
- نمایش LONG / SHORT.
- نمایش SL / TP.
- مشخص بودن محل باز و بسته شدن Position روی نمودار.
- نمایش داده‌های بازار بدون نمودار ساده و بی‌روح.

## 2.3 Equity Curve و Metrics

- Equity Curve.
- Balance/Equity history.
- ROI.
- Net PnL.
- Win Rate.
- Max Drawdown.
- Profit Factor.
- Total Fees.
- Funding.
- خلاصه قابل فهم عملکرد استراتژی.

## 2.4 Trade Report

برای هر معامله باید در صورت وجود داده نمایش داده شود:

- شماره معامله.
- Long/Short.
- Entry price.
- Exit price.
- Timeframe.
- Entry/Exit time.
- Position size.
- Leverage.
- SL.
- TP.
- Reason for exit.
- آیا SL لمس شده است یا خیر.
- PnL.
- PnL percentage.
- Fees.
- Funding.
- وضعیت Win/Loss.

### وضعیت فعلی 2.4

🟢 **Complete** — مدل `TradeResult` و صفحه `TradeByTradeReport` موجود هستند و تمام فیلدهای الزامی Trade Report را مصرف می‌کنند. موتور Backtest نیز `slTouched` را از رفتار واقعی کندل‌ها دنبال می‌کند. تست قرارداد Trade-by-Trade و تست مدل اجرا شده‌اند. این وضعیت بر اساس موفقیت واقعی Unit Test و `assembleDebug` در GitHub Actions Run #445 ثبت شده است.

---

# 3. Strategy Manager و Robot Import

- مدیریت و انتخاب Strategy.
- حفظ Strategyهای موجود.
- تنظیم پارامترهای Strategy.
- امکان توسعه برای Strategyهای جدید.
- Robot/Strategy Import باید حفظ و تکمیل شود.
- قابلیت اضافه‌کردن Strategy جدید نباید قابلیت‌های قبلی را خراب کند.

---

# 4. تنظیمات معامله

کاربر باید بتواند در صورت پشتیبانی مدل/Strategy مقدارهای زیر را تغییر دهد:

- Trade amount.
- Leverage.
- Timeframe.
- تنظیمات مرتبط با Backtest.
- تنظیمات عمومی اپ.

---

# 5. CoinEx و Market Data

- معماری آماده برای داده‌های CoinEx.
- Historical market data برای Backtest.
- Coin price connectivity.
- Live market data architecture.
- Live و non-live mode در جایی که قابلیت وجود دارد باید از هم مشخص باشند.
- داده واقعی نباید با داده ساختگی نمایش داده شود.
- در صورت نبود داده واقعی، وضعیت باید صریحاً مشخص شود.

---

# 6. Market Intelligence

## 6.1 Market Trend

- صفحه/بخش مستقل Market Trend.
- وضعیت روند بازار.
- سیگنال‌ها و ریسک‌ها در صورت وجود داده معتبر.

## 6.2 Pump / Dump Detector

- تشخیص Pump/Dump.
- Risk signals.
- Score و توضیح قابل فهم.

## 6.3 Whale / Smart Money

- Whale activity.
- Smart-money signals.
- Risk signals.
- تفکیک داده واقعی از Unknown.

## 6.4 Meme / Shitcoin Scanner

- Classification.
- Risk score.
- Signals.
- Integration با Coin Intelligence.

## 6.5 Coin Intelligence Engine

باید بتواند سیگنال‌ها و نتایج بخش‌های مختلف را بدون ایجاد مدل موازی و تکراری در یک ارزیابی واحد جمع کند.

---

# 7. Project / Product Research — آیتم‌های 105–108

## 105 — Project Overview

برای هر پروژه، در صورت وجود منبع معتبر:

- Name.
- Symbol.
- Chain / Network.
- Contract address.
- Category.
- Project type.
- Description.
- Objective.
- Problem / Solution.
- Website.
- Whitepaper.
- Official social links.
- GitHub.
- Explorer.
- Project status.
- Source.
- Timestamp.

**داده نامعلوم باید `Unknown` بماند و هرگز ساخته نشود.**

## 106 — Product / Utility Analysis

- محصول و کاربرد واقعی.
- Use case.
- Target users.
- Utility.
- ضرورت Token.
- Token use cases.
- Product maturity.
- Mainnet/Testnet.
- Live vs Idea.
- Strengths.
- Weaknesses.
- Product risks.
- Score.
- Confidence.

## 107 — Project Activity / Development

در صورت وجود داده معتبر:

- Latest release.
- Latest commit.
- Commit trend.
- Contributors.
- Issues / PR activity.
- Release history.
- Last activity.
- Development score.
- Development risk.

داده ناقص باید صریحاً مشخص شود.

## 108 — Research Summary

ترکیب 105–107 شامل:

- Project summary.
- Product summary.
- Development summary.
- Strengths.
- Weaknesses.
- Risks.
- Data gaps.
- Overall score.
- Confidence.
- Sources.
- Timestamp.

خروجی باید قابل مصرف توسط Coin Intelligence Engine باشد.

---

# 8. Team / Founder / Investors — آیتم‌های 109–111

## 109 — Team / Founder Intelligence

- Founder / Co-founders / Core team.
- Roles.
- Official profiles در صورت وجود.
- Previous experience فقط با منبع معتبر.
- Anonymity status.
- Transparency score.
- Team risk.
- Confidence.
- Sources.

## 110 — Investors / Backers

- Investors.
- VC / Funds.
- Strategic investors.
- Funding rounds.
- Round type.
- Amount/date در صورت وجود.
- Lead investor.
- Other backers.
- Source.
- Confidence.
- تفکیک `Confirmed` / `Reported` / `Unknown`.

## 111 — Team / Investor Assessment

- Team score.
- Transparency score.
- Investor quality.
- Funding strength.
- Reputation signals.
- Risk flags.
- Anonymous-team flag.
- Unknown-data flag.
- Overall assessment.
- Confidence.

---

# 9. Roadmap / Milestones — آیتم‌های 112–113

## 112 — Roadmap Engine

- Milestone title.
- Description.
- Target date.
- Status: completed / in progress / upcoming / delayed / unknown.
- Source.
- Last update.
- آیا Roadmap وجود دارد؟
- آیا رسمی است یا Third-party؟

## 113 — Milestone Analysis

- Completion rate.
- Delay rate.
- Upcoming milestones.
- Missed milestones.
- Development progress.
- Roadmap credibility.
- Progress score.
- Risk flags.

خروجی باید به Coin Intelligence متصل شود.

---

# 10. Tokenomics — آیتم‌های 114–116

## 114 — Supply Structure

- Circulating supply.
- Total supply.
- Max supply.
- Initial supply.
- Fully diluted supply.
- Inflationary / Deflationary.
- Supply change.
- Circulating percentage.
- Remaining supply.
- Source / Timestamp.
- Low circulation detection.
- Large future unlock detection.
- Unlimited/unknown supply detection.

## 115 — Token Allocation

دسته‌های احتمالی:

- Team.
- Advisors.
- Investors.
- Private sale.
- Public sale.
- Community.
- Ecosystem.
- Treasury.
- Marketing.
- Liquidity.
- Staking.
- Airdrop.
- Foundation.
- Other.

برای هر مورد در صورت وجود:

- Percentage.
- Amount.
- Unlock/Vesting.
- Source.
- Confidence.

جمع تخصیص‌ها باید قابل اعتبارسنجی باشد.

## 116 — Tokenomics Assessment

- Supply risk.
- Allocation risk.
- Investor/team allocation risk.
- Inflation risk.
- Dilution risk.
- Concentration risk.
- Utility risk.
- Tokenomics score.
- Risk score.
- Confidence.
- Explainable findings.

---

# 11. Unlock / Vesting / Burn / Emission — آیتم 117

باید در صورت وجود داده معتبر پشتیبانی شود:

- Unlock date.
- Unlock amount.
- Unlock percentage.
- Category.
- Next unlock.
- Future unlocks.
- Cliff.
- Vesting start/end.
- Vesting schedule.
- Burn amount.
- Burn rate.
- Burn events.
- Burn mechanism.
- Emission rate.
- Emission schedule.
- Inflation.
- Supply pressure risk.
- Dilution risk.
- Unlock risk.
- Emission risk.
- Burn-offset risk.
- Overall unlock score.

---

# 12. On-chain / Holder Concentration — آیتم 118

در صورت وجود داده معتبر:

- Holder count.
- Top 10/20/50/100 holders.
- Concentration.
- Largest holders.
- Exchange wallets.
- Burn wallets.
- Treasury wallets.
- Team wallets.
- Smart-money wallets.
- Distribution.
- Whale concentration.
- Holder-growth trend.
- Concentration/distribution risk.
- Scores.

**Exchange، Burn و Treasury walletها نباید کورکورانه به‌عنوان Holder عادی محاسبه شوند.**

# 13. Independent AI Hub — آیتم 119

AI Hub باید یک Workspace مستقل از Backtester باشد، اما بتواند داده‌های معتبر Backtest، Market Intelligence و Research را در صورت وجود دریافت و تحلیل کند.

---

## Change Log / وضعیت‌های تأییدشده

### 2026-09-09 — Trade Report
- مدل `TradeResult` دارای داده‌های کامل معامله شامل timeframe، leverage و وضعیت `slTouched` است.
- موتور Backtest مقدار `slTouched` را از رفتار واقعی کندل‌ها دنبال می‌کند.
- صفحه `TradeByTradeReport` به `TerminalPage.REPORT` متصل است و گزارش معامله را از `state.report` واقعی نمایش می‌دهد.
- پوشش تست مدل و گزارش اضافه شده است.
- CSV/JSON Export با فیلدهای کامل Trade Report در حال تکمیل/Verification است و تا موفقیت Build/Test نباید Complete تلقی شود.

### 2026-09-09 — Trade Report UI Verification
- بررسی مجدد Repository نشان داد ساخت مجدد صفحه گزارش لازم نیست؛ `TradeByTradeReport.kt` از قبل وجود دارد.
- UI موجود فیلدهای LONG/SHORT، Entry، Exit، Timeframe، Entry/Exit time، Position size، Leverage، SL، TP، Exit reason، SL touched، PnL، PnL percentage، Fees، Funding و Win/Loss را نمایش می‌دهد.
- تست قرارداد نهایی `TradeByTradeReportTest.kt` برای کنترل اتصال داده‌های الزامی اضافه شده است.
- وضعیت 2.4 تا موفقیت Unit Test و `assembleDebug`: 🟡 **در حال Verification**.

### 2026-09-12 — Trade Report + CSV/JSON Export Verification
- GitHub Actions Run #445 با Run ID `34672467336` با موفقیت کامل شد.
- Unit Test با موفقیت اجرا شد و مجموعه تست‌ها بدون Failure پایان یافت.
- `assembleDebug` با موفقیت انجام شد.
- مرحله Inspect APK size با موفقیت انجام شد.
- Artifact با موفقیت Upload شد.
- JSON Export از `org.json` Android جدا شد و به `kotlinx.serialization` تغییر یافت تا Unit Test روی JVM بدون Runtime Exception اجرا شود.
- CSV/JSON Export تمام فیلدهای لازم Trade Report را تولید می‌کند، از جمله tradeNumber، side، entry/exit، timeframe، timeها، position size، leverage، SL/TP، exit reason، slTouched، gross/net PnL، pnlPercent، fees، funding و status.
- **وضعیت 2.4 Trade Report: 🟢 Complete**.
- **وضعیت CSV Export: 🟢 Complete**.
- **وضعیت JSON Export: 🟢 Complete**.
- این تکمیل بر اساس کد موجود + تست موفق + Build موفق ثبت شده و نیازمند تأیید جداگانه روی دستگاه واقعی نیست مگر برای Verification بصری UI.
