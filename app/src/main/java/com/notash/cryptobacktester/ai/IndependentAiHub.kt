package com.notash.cryptobacktester.ai

import com.notash.cryptobacktester.intelligence.*

/** Provider-neutral boundary: domain code never depends on OpenAI/Gemini/local SDKs. */
interface AiProvider {
    val id: String
    suspend fun complete(prompt: String): AiCompletion
}
data class AiCompletion(val text: String, val provider: String, val isReal: Boolean, val confidence: Double = 0.0, val sources: List<SourceRef> = emptyList())
class FallbackAiProvider : AiProvider {
    override val id = "fallback"
    override suspend fun complete(prompt: String) = AiCompletion("AI provider is not connected. This is a deterministic fallback; no real AI analysis was performed.", id, false, 0.0)
}

data class AiHubContext(
    val research: ResearchSummary? = null,
    val team: TeamIntelligence? = null,
    val investors: InvestorAssessment? = null,
    val roadmap: RoadmapAnalysis? = null,
    val tokenomics: TokenomicsAssessment? = null,
    val unlocks: UnlockAnalysis? = null,
    val onChain: OnChainAnalysis? = null,
    val marketSummary: String? = null,
    val pumpDumpSummary: String? = null,
    val whaleSummary: String? = null,
    val memeSummary: String? = null,
    val backtestSummary: String? = null
)
data class AiHubReport(val mode: String, val title: String, val narrative: String, val findings: List<String>, val risks: List<String>, val confidence: Double, val realAi: Boolean, val sources: List<SourceRef>, val dataGaps: List<String>)

enum class EducationLevel { SIMPLE, INTERMEDIATE, ADVANCED }
enum class AiHubMode { CHAT, COIN, STRATEGY, TRADE, RISK, RESEARCH, EDUCATION }

class IndependentAiHub(private val provider: AiProvider = FallbackAiProvider()) {
    suspend fun chat(message: String, context: AiHubContext = AiHubContext()): AiHubReport = generate(AiHubMode.CHAT, message, context)
    suspend fun analyzeCoin(symbol: String, context: AiHubContext): AiHubReport = generate(AiHubMode.COIN, "Analyze $symbol", context)
    suspend fun analyzeStrategy(summary: String, context: AiHubContext): AiHubReport = generate(AiHubMode.STRATEGY, summary, context)
    suspend fun analyzeTrade(trade: String, context: AiHubContext): AiHubReport = generate(AiHubMode.TRADE, trade, context)
    suspend fun analyzeRisk(context: AiHubContext): AiHubReport = generate(AiHubMode.RISK, "Risk assessment", context)
    suspend fun research(question: String, context: AiHubContext): AiHubReport = generate(AiHubMode.RESEARCH, question, context)
    suspend fun educate(topic: String, level: EducationLevel): AiHubReport = generate(AiHubMode.EDUCATION, "$level: $topic", AiHubContext())

    private suspend fun generate(mode: AiHubMode, prompt: String, context: AiHubContext): AiHubReport {
        val gaps = mutableListOf<String>()
        if (context.research == null) gaps += "PROJECT_RESEARCH"
        if (context.tokenomics == null) gaps += "TOKENOMICS"
        if (context.onChain == null) gaps += "ONCHAIN"
        if (context.team == null) gaps += "TEAM"
        val risks = buildList {
            context.research?.risks?.let { addAll(it) }
            context.tokenomics?.findings?.let { addAll(it) }
            context.unlocks?.risks?.let { addAll(it) }
            context.onChain?.findings?.let { addAll(it) }
            context.team?.let { if (it.anonymous) add("ANONYMOUS_TEAM") }
        }.distinct()
        val sources = buildList {
            context.research?.sources?.let { addAll(it) }
            context.tokenomics?.supply?.source?.let { add(it) }
            context.onChain?.snapshot?.source?.let { add(it) }
        }.distinct()
        val promptWithData = "$prompt\nData gaps: ${gaps.joinToString()}\nRisks: ${risks.joinToString()}"
        val completion = provider.complete(promptWithData)
        return AiHubReport(mode.name, "Notash Independent AI Hub", completion.text, risks, risks, completion.confidence, completion.isReal, sources + completion.sources, gaps)
    }
}
