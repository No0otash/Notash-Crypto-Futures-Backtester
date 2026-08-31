package com.notash.cryptobacktester.ai

import com.notash.cryptobacktester.intelligence.*

/** Provider-neutral AI boundary. Real AI is never implied when no provider is configured. */
interface AiProvider {
    val id: String
    suspend fun complete(prompt: String): AiCompletion
}

data class AiCompletion(val text: String, val provider: String, val isReal: Boolean, val confidence: Double = 0.0, val sources: List<SourceRef> = emptyList())

class FallbackAiProvider : AiProvider {
    override val id = "fallback"
    override suspend fun complete(prompt: String) = AiCompletion(
        "AI provider is not connected. This deterministic fallback did not perform real AI analysis.", id, false, 0.0
    )
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

data class AiHubReport(
    val mode: String, val title: String, val narrative: String, val findings: List<String>,
    val risks: List<String>, val confidence: Double, val realAi: Boolean,
    val sources: List<SourceRef>, val dataGaps: List<String>
)

enum class EducationLevel { SIMPLE, INTERMEDIATE, ADVANCED }
enum class AiHubMode { CHAT, COIN, STRATEGY, TRADE, RISK, RESEARCH, EDUCATION }

class IndependentAiHub(private val provider: AiProvider = FallbackAiProvider()) {
    suspend fun chat(message: String, context: AiHubContext = AiHubContext()) = generate(AiHubMode.CHAT, message, context)
    suspend fun analyzeCoin(symbol: String, context: AiHubContext) = generate(AiHubMode.COIN, "Analyze $symbol", context)
    suspend fun analyzeStrategy(summary: String, context: AiHubContext) = generate(AiHubMode.STRATEGY, summary, context)
    suspend fun analyzeTrade(trade: String, context: AiHubContext) = generate(AiHubMode.TRADE, trade, context)
    suspend fun analyzeRisk(context: AiHubContext) = generate(AiHubMode.RISK, "Risk assessment", context)
    suspend fun research(question: String, context: AiHubContext) = generate(AiHubMode.RESEARCH, question, context)
    suspend fun educate(topic: String, level: EducationLevel) = generate(AiHubMode.EDUCATION, "$level: $topic", AiHubContext())

    private suspend fun generate(mode: AiHubMode, prompt: String, context: AiHubContext): AiHubReport {
        val gaps = buildList {
            if (context.research == null) add("PROJECT_RESEARCH")
            if (context.tokenomics == null) add("TOKENOMICS")
            if (context.onChain == null) add("ONCHAIN")
            if (context.team == null) add("TEAM")
        }
        val risks = buildList {
            context.research?.risks?.let(::addAll)
            context.tokenomics?.findings?.let(::addAll)
            context.unlocks?.risks?.let(::addAll)
            context.onChain?.findings?.let(::addAll)
            if (context.team?.anonymous == true) add("ANONYMOUS_TEAM")
        }.distinct()
        val sources = buildList {
            context.research?.sources?.let(::addAll)
            context.tokenomics?.supply?.source?.let(::add)
            context.onChain?.snapshot?.source?.let(::add)
        }.distinct()
        val completion = provider.complete("$prompt\nData gaps: ${gaps.joinToString()}\nRisks: ${risks.joinToString()}")
        return AiHubReport(mode.name, "ALVEX Independent AI Hub", completion.text, risks, risks, completion.confidence, completion.isReal, sources + completion.sources, gaps)
    }
}
