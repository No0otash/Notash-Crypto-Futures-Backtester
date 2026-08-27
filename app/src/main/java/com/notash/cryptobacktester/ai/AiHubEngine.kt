package com.notash.cryptobacktester.ai

/**
 * Independent, deterministic AI Hub foundation.
 * The engine intentionally avoids inventing market facts when live data is absent.
 * A future remote AI provider can consume the generated context without changing the UI.
 */
object AiHubEngine {
    data class Answer(
        val title: String,
        val body: String,
        val confidence: String = "High"
    )

    fun answer(question: String): Answer {
        val q = question.trim().lowercase()
        if (q.isBlank()) return Answer("AI Hub", "سؤال خود را وارد کنید.")
        return when {
            "funding" in q -> Answer(
                "Funding Rate",
                "Funding rate پرداخت دوره‌ای بین معامله‌گران Long و Short است. مثبت بودن آن معمولاً یعنی Longها پرداخت می‌کنند و منفی بودن یعنی Shortها پرداخت می‌کنند. Funding به‌تنهایی سیگنال خرید یا فروش نیست و باید همراه با Open Interest، قیمت و حجم بررسی شود."
            )
            "long" in q && "short" in q -> Answer(
                "Long vs Short",
                "Long از افزایش قیمت سود می‌گیرد و Short از کاهش قیمت. در معاملات Futures، اهرم سود و زیان را نسبت به سرمایه افزایش می‌دهد؛ بنابراین اندازه پوزیشن و Stop Loss اهمیت زیادی دارند."
            )
            "tokenomics" in q || "توکنوم" in q -> Answer(
                "Tokenomics",
                "برای تحلیل Tokenomics باید Max Supply، Total Supply، Circulating Supply، Unlock/Vesting، سهم Team و Investors، Treasury، Ecosystem، Burn و Emission بررسی شود. نبود داده معتبر نباید به امتیاز مثبت تبدیل شود.",
                "High"
            )
            "risk" in q || "ریسک" in q -> Answer(
                "Risk",
                "ریسک را فقط با نوسان قیمت نسنجید. Liquidity، leverage، drawdown، concentration، unlockهای نزدیک، رفتار whaleها و ریسک قرارداد هوشمند هم باید بررسی شوند."
            )
            "backtest" in q || "بک تست" in q -> Answer(
                "Backtest",
                "بک‌تست باید کارمزد، funding، slippage، ورود و خروج، SL/TP، اندازه پوزیشن و محدودیت اهرم را در نظر بگیرد. نتیجه تاریخی تضمین‌کننده عملکرد آینده نیست."
            )
            else -> Answer(
                "AI Hub",
                "سؤال دریافت شد. برای پاسخ داده‌محور درباره یک کوین، نام نماد و داده‌های بازار/آن‌چین را نیز وارد کنید. بدون داده معتبر، AI نباید عدد یا ادعای بازار را حدس بزند.",
                "Medium"
            )
        }
    }

    fun buildCoinAnalysisPrompt(symbol: String, context: Map<String, String>): String {
        val lines = context.entries.joinToString("\n") { "${it.key}: ${it.value}" }
        return """
            Analyze $symbol as a crypto asset.
            Never invent missing facts. Mark missing evidence as Data Confidence: Low.
            Evaluate Product/Utility, Team, Investors, Roadmap, Tokenomics, Unlock Risk,
            Burn/Emission, On-chain, Market Trend, Liquidity and Narrative.
            Identify early-growth evidence separately from already-pumped price action.
            Return Growth Score, reasons, risks, Bull/Base/Bear scenarios and data confidence.

            Available evidence:
            $lines
        """.trimIndent()
    }
}
