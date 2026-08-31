package com.notash.cryptobacktester.intelligence

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class HuntFloTelegramParserTest {
    @Test
    fun parsesWhaleMessageAndIgnoresPriceOnlyPost() {
        val html = """
            <div class="tgme_widget_message_wrap"><div class="tgme_widget_message_text">🐋 Whale transferred 1,250,000 USDT to Binance inflow</div><time datetime="2026-08-31T10:00:00+00:00"></time></div>
            <div class="tgme_widget_message_wrap"><div class="tgme_widget_message_text">BTC price is trading at 78,200 USD, 24h +1.2%</div><time datetime="2026-08-31T10:01:00+00:00"></time></div>
        """.trimIndent()
        val events = HuntFloTelegramParser.parse(html, "https://t.me/s/HuntFlo")
        assertEquals(1, events.size)
        assertEquals("HuntFlo Telegram", events.single().source)
        assertEquals(WhaleFlow.EXCHANGE_INFLOW, events.single().flow)
        assertEquals(1250000.0, events.single().amount)
    }

    @Test
    fun emptyPageProducesNoEvents() {
        assertEquals(emptyList(), HuntFloTelegramParser.parse("", "https://t.me/s/HuntFlo"))
        assertNull(HuntFloMessageClassifier.parse("ETH price is 3000 USD", 1L))
    }
}
