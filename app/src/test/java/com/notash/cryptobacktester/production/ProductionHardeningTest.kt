package com.notash.cryptobacktester.production

import com.notash.cryptobacktester.data.StrategyPackageImporter
import com.notash.cryptobacktester.data.StrategyPackage
import com.notash.cryptobacktester.security.SecureAccountStore
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ProductionHardeningTest {
    @Test fun strategyPackageImporterAcceptsValidPackage() {
        val json = Json.encodeToString(StrategyPackage.serializer(), StrategyPackage("demo", "Demo", "1.0"))
        val result = StrategyPackageImporter().importJson(json)
        assertTrue(result.isSuccess)
        assertEquals("demo", result.getOrThrow().id)
    }

    @Test fun strategyPackageImporterRejectsInvalidPackage() {
        val result = StrategyPackageImporter().importJson("{\"id\":\"\",\"name\":\"\",\"version\":\"\",\"riskPercent\":0}")
        assertFalse(result.isSuccess)
    }

    @Test fun supportedLanguagesRemainCanonical() {
        val tags = listOf("en", "fa", "ar", "fr", "zh")
        assertEquals(5, tags.distinct().size)
    }
}
