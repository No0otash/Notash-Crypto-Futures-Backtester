package com.notash.cryptobacktester.strategy

data class StrategyPackage(val id:String,val name:String,val version:String,val entryRules:String,val exitRules:String,val riskRules:String)

object StrategyPackageValidator {
    fun validate(p: StrategyPackage): List<String> = buildList {
        if (p.id.isBlank()) add("Strategy ID is required")
        if (p.name.isBlank()) add("Strategy name is required")
        if (p.version.isBlank()) add("Strategy version is required")
        if (p.entryRules.isBlank()) add("Entry rules are required")
        if (p.exitRules.isBlank()) add("Exit rules are required")
    }
}
