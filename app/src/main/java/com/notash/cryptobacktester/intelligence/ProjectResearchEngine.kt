package com.notash.cryptobacktester.intelligence

/** Provider-neutral research models. Missing fields intentionally remain null/Unknown. */
data class SourceRef(val title: String, val url: String? = null, val timestamp: String? = null, val status: SourceStatus = SourceStatus.UNKNOWN)
enum class SourceStatus { CONFIRMED, REPORTED, UNKNOWN }

data class ProjectProfile(
    val name: String = "Unknown", val symbol: String = "Unknown", val chain: String = "Unknown",
    val contractAddress: String = "Unknown", val category: String = "Unknown", val projectType: String = "Unknown",
    val description: String = "Unknown", val objective: String = "Unknown", val problem: String = "Unknown",
    val solution: String = "Unknown", val website: String? = null, val whitepaper: String? = null,
    val officialSocials: List<String> = emptyList(), val github: String? = null, val explorer: String? = null,
    val status: String = "Unknown", val source: SourceRef? = null, val timestamp: String? = null
)

data class ProductAnalysis(
    val realWorldUseCase: String = "Unknown", val targetUsers: String = "Unknown", val utility: String = "Unknown",
    val tokenNecessary: Boolean? = null, val tokenUseCases: List<String> = emptyList(), val maturity: String = "Unknown",
    val mainnet: Boolean? = null, val liveProduct: Boolean? = null, val strengths: List<String> = emptyList(),
    val weaknesses: List<String> = emptyList(), val risks: List<String> = emptyList(), val score: Double = 0.0,
    val confidence: Double = 0.0, val sources: List<SourceRef> = emptyList()
)

data class DevelopmentActivity(
    val latestRelease: String? = null, val latestCommit: String? = null, val commitTrend: String = "Unknown",
    val contributors: Int? = null, val openIssues: Int? = null, val openPullRequests: Int? = null,
    val releaseHistory: List<String> = emptyList(), val lastActivity: String? = null, val score: Double = 0.0,
    val risk: String = "Unknown", val sources: List<SourceRef> = emptyList()
)

data class ResearchSummary(
    val profile: ProjectProfile, val product: ProductAnalysis, val development: DevelopmentActivity,
    val strengths: List<String>, val weaknesses: List<String>, val risks: List<String>, val dataGaps: List<String>,
    val overallScore: Double, val confidence: Double, val sources: List<SourceRef>, val timestamp: String
)

data class TeamMember(val name: String, val role: String = "Unknown", val profileUrl: String? = null, val experience: String = "Unknown", val source: SourceRef? = null)
data class TeamIntelligence(val members: List<TeamMember> = emptyList(), val anonymous: Boolean = true, val transparencyScore: Double = 0.0, val teamRisk: Double = 100.0, val confidence: Double = 0.0, val sources: List<SourceRef> = emptyList())
data class Investor(val name: String, val round: String = "Unknown", val amount: String = "Unknown", val date: String = "Unknown", val lead: Boolean? = null, val status: SourceStatus = SourceStatus.UNKNOWN, val source: SourceRef? = null)
data class InvestorAssessment(val investors: List<Investor> = emptyList(), val qualityScore: Double = 0.0, val fundingStrength: Double = 0.0, val reputationScore: Double = 0.0, val risks: List<String> = emptyList(), val confidence: Double = 0.0, val sources: List<SourceRef> = emptyList())
data class TeamInvestorAssessment(val teamScore: Double, val transparencyScore: Double, val investorQuality: Double, val fundingStrength: Double, val reputation: Double, val riskFlags: List<String>, val anonymousTeam: Boolean, val unknownData: Boolean, val confidence: Double)

enum class MilestoneStatus { COMPLETED, IN_PROGRESS, UPCOMING, DELAYED, UNKNOWN }
data class RoadmapMilestone(val title: String, val description: String = "Unknown", val targetDate: String? = null, val status: MilestoneStatus = MilestoneStatus.UNKNOWN, val source: SourceRef? = null, val lastUpdate: String? = null)
data class RoadmapAnalysis(val available: Boolean, val official: Boolean?, val milestones: List<RoadmapMilestone>, val completionRate: Double, val delayRate: Double, val upcoming: List<String>, val missed: List<String>, val progressScore: Double, val credibilityScore: Double, val riskFlags: List<String>)

data class TokenSupply(val circulating: Double? = null, val total: Double? = null, val max: Double? = null, val initial: Double? = null, val fullyDiluted: Double? = null, val inflationary: Boolean? = null, val supplyChange: Double? = null, val source: SourceRef? = null, val timestamp: String? = null)
data class Allocation(val category: String, val percentage: Double? = null, val amount: Double? = null, val unlock: String = "Unknown", val vesting: String = "Unknown", val source: SourceRef? = null, val confidence: Double = 0.0)
data class TokenomicsAssessment(val supply: TokenSupply, val allocations: List<Allocation>, val allocationTotal: Double?, val lowCirculation: Boolean, val largeFutureUnlock: Boolean, val unlimitedOrUnknownSupply: Boolean, val concentrationRisk: Double, val inflationRisk: Double, val dilutionRisk: Double, val utilityRisk: Double, val score: Double, val riskScore: Double, val confidence: Double, val findings: List<String>)
data class UnlockEvent(val date: String, val amount: Double? = null, val percentage: Double? = null, val category: String = "Unknown", val cliff: String? = null, val vestingStart: String? = null, val vestingEnd: String? = null, val source: SourceRef? = null)
data class BurnEvent(val date: String, val amount: Double? = null, val mechanism: String = "Unknown", val source: SourceRef? = null)
data class EmissionSchedule(val rate: Double? = null, val unit: String = "Unknown", val schedule: String = "Unknown", val source: SourceRef? = null)
data class UnlockAnalysis(val events: List<UnlockEvent>, val nextUnlock: UnlockEvent?, val futureUnlocks: List<UnlockEvent>, val burnEvents: List<BurnEvent>, val emission: EmissionSchedule?, val supplyPressure: Double, val dilutionRisk: Double, val emissionRisk: Double, val burnOffsetRisk: Double, val overallScore: Double, val risks: List<String>)

data class HolderSnapshot(val holderCount: Long? = null, val top10Percent: Double? = null, val top20Percent: Double? = null, val top50Percent: Double? = null, val top100Percent: Double? = null, val largestHolderPercent: Double? = null, val exchangeWallets: List<String> = emptyList(), val burnWallets: List<String> = emptyList(), val treasuryWallets: List<String> = emptyList(), val teamWallets: List<String> = emptyList(), val smartMoneyWallets: List<String> = emptyList(), val distribution: String = "Unknown", val holderGrowthTrend: String = "Unknown", val source: SourceRef? = null)
data class OnChainAnalysis(val snapshot: HolderSnapshot, val concentrationRisk: Double, val distributionScore: Double, val whaleRisk: Double, val holderGrowthScore: Double, val overallScore: Double, val confidence: Double, val findings: List<String>)

class ProjectResearchEngine {
    fun summarize(profile: ProjectProfile, product: ProductAnalysis, development: DevelopmentActivity, timestamp: String): ResearchSummary {
        val gaps = mutableListOf<String>()
        if (profile.website == null) gaps += "WEBSITE"
        if (profile.github == null) gaps += "GITHUB"
        if (development.latestCommit == null) gaps += "LATEST_COMMIT"
        if (product.utility == "Unknown") gaps += "UTILITY"
        val score = listOf(product.score, development.score).filter { it > 0 }.let { if (it.isEmpty()) 0.0 else it.average() }
        val confidence = listOf(product.confidence, if (development.sources.isNotEmpty()) 80.0 else 0.0).average()
        return ResearchSummary(profile, product, development, product.strengths, product.weaknesses, product.risks, gaps, score, confidence, (listOfNotNull(profile.source) + product.sources + development.sources).distinct(), timestamp)
    }
}

class RoadmapEngine {
    fun analyze(milestones: List<RoadmapMilestone>, official: Boolean? = null): RoadmapAnalysis {
        if (milestones.isEmpty()) return RoadmapAnalysis(false, official, emptyList(), 0.0, 0.0, emptyList(), emptyList(), 0.0, 0.0, listOf("ROADMAP_UNAVAILABLE"))
        val completed = milestones.count { it.status == MilestoneStatus.COMPLETED }
        val delayed = milestones.count { it.status == MilestoneStatus.DELAYED }
        val done = completed.toDouble() / milestones.size * 100.0
        val delay = delayed.toDouble() / milestones.size * 100.0
        val risks = buildList { if (delayed > 0) add("DELAYED_MILESTONES"); if (official != true) add("ROADMAP_SOURCE_NOT_CONFIRMED_OFFICIAL") }
        return RoadmapAnalysis(true, official, milestones, done, delay, milestones.filter { it.status == MilestoneStatus.UPCOMING }.map { it.title }, milestones.filter { it.status == MilestoneStatus.DELAYED }.map { it.title }, (done - delay * .5).coerceIn(0.0, 100.0), (if (official == true) 80.0 else 40.0), risks)
    }
}

class TokenomicsEngine {
    fun assess(supply: TokenSupply, allocations: List<Allocation>): TokenomicsAssessment {
        val total = allocations.mapNotNull { it.percentage }.sum().takeIf { allocations.any { a -> a.percentage != null } }
        val low = when { supply.circulating == null || supply.total == null || supply.total <= 0 -> true; else -> supply.circulating / supply.total < .20 }
        val unknownSupply = supply.max == null && supply.total == null
        val largeUnlock = allocations.any { (it.category.equals("investor", true) || it.category.equals("team", true)) && (it.percentage ?: 0.0) >= 15.0 && it.unlock != "Unknown" }
        val concentration = ((allocations.filter { it.category.lowercase() in setOf("team","investor","private sale","treasury") }.sumOf { it.percentage ?: 0.0 }) * 1.5).coerceIn(0.0, 100.0)
        val inflation = if (supply.inflationary == true) 70.0 else if (supply.inflationary == false) 20.0 else 50.0
        val dilution = if (low || largeUnlock) 75.0 else 25.0
        val utility = if (allocations.isEmpty()) 70.0 else 35.0
        val risk = (concentration*.35 + inflation*.2 + dilution*.35 + utility*.1).coerceIn(0.0,100.0)
        val score = 100.0-risk
        val confidence = allocations.map { it.confidence }.takeIf { it.isNotEmpty() }?.average() ?: 0.0
        val findings = buildList { if (low) add("LOW_CIRCULATION"); if (largeUnlock) add("LARGE_FUTURE_UNLOCK"); if (unknownSupply) add("UNLIMITED_OR_UNKNOWN_SUPPLY"); if (total != null && kotlin.math.abs(total-100.0) > 1.0) add("ALLOCATION_TOTAL_NOT_100_PERCENT") }
        return TokenomicsAssessment(supply, allocations, total, low, largeUnlock, unknownSupply, concentration, inflation, dilution, utility, score, risk, confidence, findings)
    }
}

class UnlockEngine {
    fun analyze(events: List<UnlockEvent>, burns: List<BurnEvent>, emission: EmissionSchedule?, nowDate: String): UnlockAnalysis {
        val sorted = events.sortedBy { it.date }
        val next = sorted.firstOrNull { it.date >= nowDate }
        val pressure = sorted.sumOf { it.percentage ?: 0.0 }.coerceIn(0.0,100.0)
        val dilution = (pressure * .8).coerceIn(0.0,100.0)
        val emissionRisk = if (emission?.rate != null) (emission.rate * 10.0).coerceIn(0.0,100.0) else 50.0
        val burn = burns.sumOf { it.amount ?: 0.0 }
        val burnOffset = if (burn > 0) 25.0 else 70.0
        val score = (100.0 - dilution*.5 - emissionRisk*.25 - burnOffset*.25).coerceIn(0.0,100.0)
        val risks = buildList { if (next != null) add("UPCOMING_UNLOCK"); if (pressure >= 20) add("HIGH_UNLOCK_PRESSURE"); if (emission?.rate != null) add("EMISSION_PRESENT"); if (burns.isEmpty()) add("NO_VERIFIED_BURN_EVENTS") }
        return UnlockAnalysis(events, next, sorted.drop(1), burns, emission, pressure, dilution, emissionRisk, burnOffset, score, risks)
    }
}

class OnChainEngine {
    fun analyze(snapshot: HolderSnapshot): OnChainAnalysis {
        val top = snapshot.top10Percent ?: snapshot.top20Percent ?: 100.0
        val concentration = top.coerceIn(0.0,100.0)
        val distribution = (100.0 - concentration).coerceIn(0.0,100.0)
        val whale = (snapshot.largestHolderPercent ?: top).coerceIn(0.0,100.0)
        val growth = when (snapshot.holderGrowthTrend.uppercase()) { "UP", "GROWING", "POSITIVE" -> 80.0; "DOWN", "DECLINING" -> 25.0; else -> 50.0 }
        val findings = buildList { if (concentration >= 50) add("HIGH_HOLDER_CONCENTRATION"); if (whale >= 20) add("LARGE_LARGEST_HOLDER"); if (snapshot.holderCount == null) add("HOLDER_COUNT_UNKNOWN"); if (snapshot.source == null) add("ONCHAIN_SOURCE_UNKNOWN") }
        return OnChainAnalysis(snapshot, concentration, distribution, whale, growth, ((100-concentration)*.5 + distribution*.2 + growth*.3).coerceIn(0.0,100.0), if (snapshot.source != null) 75.0 else 0.0, findings)
    }
}
