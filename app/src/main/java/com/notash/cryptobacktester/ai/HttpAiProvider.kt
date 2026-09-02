package com.notash.cryptobacktester.ai

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

/** Real network AI provider. The endpoint and secret are supplied by the host app/backend. */
class HttpAiProvider(
    private val endpoint: String,
    private val apiKey: String,
    private val client: OkHttpClient = OkHttpClient()
) : AiProvider {
    override val id: String = "http-ai"

    override suspend fun complete(prompt: String): AiCompletion {
        require(endpoint.startsWith("https://")) { "AI endpoint must use HTTPS" }
        require(apiKey.isNotBlank()) { "AI provider key is required" }
        val payload = JSONObject().put("prompt", prompt).toString()
        val request = Request.Builder()
            .url(endpoint)
            .header("Authorization", "Bearer $apiKey")
            .header("Accept", "application/json")
            .post(payload.toRequestBody("application/json".toMediaType()))
            .build()
        client.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            check(response.isSuccessful) { "AI provider HTTP ${response.code}" }
            val json = JSONObject(body)
            val text = json.optString("text", json.optString("response", json.optString("content", "")))
            check(text.isNotBlank()) { "AI provider returned no completion" }
            return AiCompletion(text, id, true, json.optDouble("confidence", 0.0).coerceIn(0.0, 1.0))
        }
    }
}
