package com.notash.cryptobacktester.intelligence

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.time.Instant

/**
 * Public Telegram channel adapter for HuntFlo. It reads the public channel preview
 * and never requires a Telegram account or stores credentials in the APK.
 * Price-only posts are filtered by HuntFloMessageClassifier.
 */
class HuntFloTelegramProvider(
    private val client: OkHttpClient = OkHttpClient(),
    private val channelUrl: String = "https://t.me/s/HuntFlo"
) : WhaleIntelligenceProvider {
    override suspend fun latestEvents(limit: Int): List<WhaleEvent> = withContext(Dispatchers.IO) {
        require(limit > 0) { "limit must be positive" }
        val request = Request.Builder().url(channelUrl).get().build()
        client.newCall(request).execute().use { response ->
            check(response.isSuccessful) { "HuntFlo request failed: HTTP ${response.code}" }
            val html = response.body?.string().orEmpty()
            HuntFloTelegramParser.parse(html, channelUrl).take(limit)
        }
    }
}

object HuntFloTelegramParser {
    private val block = Regex(
        "<div[^>]*class=\\\"[^\\\"]*tgme_widget_message_wrap[^\\\"]*\\\"[^>]*>(.*?)(?=<div[^>]*class=\\\"[^\\\"]*tgme_widget_message_wrap|</body>)",
        setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE)
    )
    private val text = Regex("<div[^>]*class=\\\"[^\\\"]*tgme_widget_message_text[^\\\"]*\\\"[^>]*>(.*?)</div>", setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE))
    private val datetime = Regex("<time[^>]*datetime=\\\"([^\\\"]+)\\\"", RegexOption.IGNORE_CASE)

    fun parse(html: String, sourceUrl: String): List<WhaleEvent> {
        if (html.isBlank()) return emptyList()
        return block.findAll(html).mapNotNull { match ->
            val body = match.groupValues[1]
            val message = text.find(body)?.groupValues?.getOrNull(1)?.let(::stripHtml)?.trim().orEmpty()
            if (message.isBlank()) return@mapNotNull null
            val observed = datetime.find(body)?.groupValues?.getOrNull(1)?.let { runCatching { Instant.parse(it).toEpochMilli() }.getOrNull() }
                ?: System.currentTimeMillis()
            HuntFloMessageClassifier.parse(message, observed)?.copy(
                source = "HuntFlo Telegram",
                evidence = listOf(IntelligenceEvidence(sourceUrl, "public-telegram-message", observed))
            )
        }.toList().asReversed()
    }

    private fun stripHtml(value: String): String = value
        .replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\\n")
        .replace(Regex("<[^>]+>"), "")
        .replace("&nbsp;", " ")
        .replace("&amp;", "&")
        .replace("&quot;", "\"")
        .replace("&#39;", "'")
        .replace(Regex("\\s+"), " ")
}
