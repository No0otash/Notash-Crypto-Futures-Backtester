package com.notash.cryptobacktester.ai

import com.notash.cryptobacktester.core.BacktestReport
import com.notash.cryptobacktester.core.Side
import com.notash.cryptobacktester.core.TradeResult
import kotlin.math.abs

object TradeAiAnalyzer {
    data class TradeDiagnosis(val index: Int, val outcome: String, val severity: String, val primaryCause: String, val evidence: List<String>, val recommendation: String)
    data class StrategyDiagnosis(val summary: String, val strengths: List<String>, val weaknesses: List<String>, val recommendations: List<String>, val healthScore: Int)
    data class Analysis(val trades: List<TradeDiagnosis>, val strategy: StrategyDiagnosis)

    fun analyze(report: BacktestReport): Analysis {
        val diagnoses = report.trades.mapIndexed { i, t -> diagnose(i + 1, t) }
        val wins = report.trades.count { it.netPnl > 0 }; val losses = report.trades.count { it.netPnl < 0 }
        val feeDrag = report.totalFees; val fundingDrag = report.trades.sumOf { abs(it.funding) }
        val strengths = mutableListOf<String>(); val weaknesses = mutableListOf<String>(); val recommendations = mutableListOf<String>()
        if (report.winRatePercent >= 50) strengths += "نرخ برد قابل قبول است." else weaknesses += "نرخ برد پایین است؛ کیفیت فیلتر ورود باید بررسی شود."
        if (report.profitFactor >= 1.5) strengths += "Profit Factor نشان‌دهنده مزیت تجمیعی مناسب است." else weaknesses += "Profit Factor ضعیف است و نسبت سود به زیان باید بهبود یابد."
        if (report.netPnl > 0) strengths += "ربات پس از کارمزد و Funding سودده بوده است." else weaknesses += "ربات پس از هزینه‌ها سودده نیست."
        if (report.maxDrawdownPercent >= 20) weaknesses += "Drawdown بالا است؛ مدیریت ریسک یا فیلتر بازار نیاز به اصلاح دارد."
        if (feeDrag > abs(report.netPnl).coerceAtLeast(1.0) * .10) { weaknesses += "کارمزد اثر قابل توجهی دارد."; recommendations += "تعداد ورودهای کم‌کیفیت را کاهش دهید و فیلتر ورود را سخت‌تر تست کنید." }
        if (fundingDrag > abs(report.netPnl).coerceAtLeast(1.0) * .10) { weaknesses += "Funding اثر قابل توجهی دارد."; recommendations += "ورودهای حساس به Funding را فیلتر و Funding-aware backtest را تست کنید." }
        if (losses > wins) recommendations += "شرایط ورود را با تمرکز روی معاملات بازنده سخت‌تر کنید؛ قبل از تغییر پارامتر، روی داده خارج از نمونه تست کنید."
        if (report.maxDrawdownPercent >= 20) recommendations += "ریسک هر معامله یا تعداد معاملات همزمان را کاهش دهید و نتیجه را دوباره بک‌تست کنید."
        if (recommendations.isEmpty()) recommendations += "پارامترها را روی داده خارج از نمونه مقایسه کنید و از بهینه‌سازی بیش از حد جلوگیری کنید."
        val score = (50 + (report.winRatePercent - 50) * .5 + (report.profitFactor.coerceIn(0.0, 3.0) - 1.0) * 15 - report.maxDrawdownPercent.coerceAtMost(40.0) * .25).toInt().coerceIn(0, 100)
        val summary = "${report.trades.size} معامله: $wins سودده، $losses زیان‌ده؛ PnL خالص ${"%.2f".format(report.netPnl)}، ROI ${"%.2f".format(report.roiPercent)}%. تشخیص سلامت ربات: $score/100."
        return Analysis(diagnoses, StrategyDiagnosis(summary, strengths, weaknesses, recommendations, score))
    }

    private fun diagnose(index: Int, t: TradeResult): TradeDiagnosis {
        val costs = abs(t.fees) + abs(t.funding); val evidence = mutableListOf<String>()
        evidence += "Gross PNL: ${"%.4f".format(t.grossPnl)}"; evidence += "Fees: ${"%.4f".format(t.fees)}"; evidence += "Funding: ${"%.4f".format(t.funding)}"; evidence += "Side: ${if (t.side == Side.LONG) "LONG" else "SHORT"}"
        return if (t.netPnl > 0) {
            val cause = if (t.grossPnl > costs) "حرکت قیمت در جهت معامله به اندازه کافی قوی بوده و هزینه‌ها را پوشش داده است." else "سود خام بسیار کم بوده و هزینه‌ها بخش زیادی از نتیجه را مصرف کرده‌اند."
            TradeDiagnosis(index, "سود", "مثبت", cause, evidence, "الگوی ورود/خروج را حفظ کنید اما پایداری آن را روی داده خارج از نمونه آزمایش کنید.")
        } else {
            val cause = when { t.grossPnl < 0 && costs > abs(t.grossPnl) * .25 -> "حرکت قیمت خلاف معامله بوده و کارمزد/Funding زیان را تشدید کرده است."; t.grossPnl < 0 -> "حرکت قیمت بعد از ورود خلاف جهت معامله بوده است؛ فیلتر ورود و شرایط بازار باید بررسی شود."; else -> "مزیت خام معامله کم بوده و هزینه‌ها آن را منفی کرده‌اند." }
            TradeDiagnosis(index, "ضرر", "منفی", cause, evidence, "شرایط ورود، فاصله SL، وضعیت روند و هزینه‌های معامله را با معاملات مشابه مقایسه و سپس پارامتر را تست کنید.")
        }
    }
}
