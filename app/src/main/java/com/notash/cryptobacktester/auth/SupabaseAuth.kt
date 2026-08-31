package com.notash.cryptobacktester.auth

import android.content.Context
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

/** Small REST client for Supabase Auth. The publishable key is safe for client use;
 * service-role credentials must never be shipped in the APK. */
class SupabaseAuth(private val context: Context) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()
    private val prefs = context.getSharedPreferences("alvex_auth", Context.MODE_PRIVATE)
    private val baseUrl = "https://lwwiwvphbrcncfbthinh.supabase.co"
    private val anonKey = "sb_publishable_6A92ZNObilrUgIRioTtOIg_QRC_vj0J"
    private val jsonType = "application/json; charset=utf-8".toMediaType()

    data class Session(val accessToken: String, val refreshToken: String, val userId: String, val email: String?)
    data class Result<T>(val value: T? = null, val error: String? = null)

    private fun save(s: Session) = prefs.edit()
        .putString("access_token", s.accessToken)
        .putString("refresh_token", s.refreshToken)
        .putString("user_id", s.userId)
        .putString("email", s.email)
        .apply()

    fun currentSession(): Session? {
        val a = prefs.getString("access_token", null) ?: return null
        val r = prefs.getString("refresh_token", null) ?: return null
        return Session(a, r, prefs.getString("user_id", "") ?: "", prefs.getString("email", null))
    }

    fun signOut() { prefs.edit().clear().apply() }

    suspend fun signIn(email: String, password: String): Result<Session> = withContext(Dispatchers.IO) {
        requestSession("/auth/v1/token?grant_type=password", JSONObject()
            .put("email", email.trim()).put("password", password))
    }

    suspend fun signUp(email: String, password: String): Result<Session> = withContext(Dispatchers.IO) {
        requestSession("/auth/v1/signup", JSONObject()
            .put("email", email.trim()).put("password", password))
    }

    suspend fun refresh(): Result<Session> = withContext(Dispatchers.IO) {
        val refresh = prefs.getString("refresh_token", null)
            ?: return@withContext Result(error = "No saved session")
        requestSession("/auth/v1/token?grant_type=refresh_token", JSONObject().put("refresh_token", refresh))
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        val body = JSONObject().put("email", email.trim()).toString().toRequestBody(jsonType)
        val request = baseRequest("/auth/v1/recover")
            .post(body).build()
        execute(request).let { if (it.first in 200..299) Result(value = Unit) else Result(error = it.second) }
    }

    suspend fun updatePassword(newPassword: String): Result<Unit> = withContext(Dispatchers.IO) {
        val token = currentSession()?.accessToken ?: return@withContext Result(error = "Please sign in again")
        val body = JSONObject().put("password", newPassword).toString().toRequestBody(jsonType)
        val request = baseRequest("/auth/v1/user", token).put(body).build()
        execute(request).let { if (it.first in 200..299) Result(value = Unit) else Result(error = it.second) }
    }

    suspend fun requestEmailChange(newEmail: String): Result<Unit> = withContext(Dispatchers.IO) {
        val token = currentSession()?.accessToken ?: return@withContext Result(error = "Please sign in again")
        val body = JSONObject().put("email", newEmail.trim()).toString().toRequestBody(jsonType)
        val request = baseRequest("/auth/v1/user", token).put(body).build()
        execute(request).let { if (it.first in 200..299) Result(value = Unit) else Result(error = it.second) }
    }

    private fun requestSession(path: String, payload: JSONObject): Result<Session> {
        val request = baseRequest(path).post(payload.toString().toRequestBody(jsonType)).build()
        val (code, text) = execute(request)
        if (code !in 200..299) return Result(error = parseError(text))
        return try {
            val o = JSONObject(text)
            val session = Session(
                accessToken = o.optString("access_token"),
                refreshToken = o.optString("refresh_token"),
                userId = o.optJSONObject("user")?.optString("id") ?: "",
                email = o.optJSONObject("user")?.optString("email")
            )
            if (session.accessToken.isBlank()) Result(error = "Authentication succeeded without a session")
            else { save(session); Result(value = session) }
        } catch (_: Exception) { Result(error = "Invalid authentication response") }
    }

    private fun baseRequest(path: String, token: String? = null): Request.Builder = Request.Builder()
        .url(baseUrl + path)
        .header("apikey", anonKey)
        .header("Accept", "application/json")
        .apply { if (token != null) header("Authorization", "Bearer $token") }

    private fun execute(request: Request): Pair<Int, String> = try {
        client.newCall(request).execute().use { response -> response.code to (response.body?.string() ?: "") }
    } catch (e: Exception) { 599 to (e.message ?: "Network error") }

    private fun parseError(text: String): String = try {
        val o = JSONObject(text)
        o.optString("msg").ifBlank { o.optString("message") }.ifBlank { "Authentication failed" }
    } catch (_: Exception) { "Authentication failed" }
}

fun sha256(value: String): String = MessageDigest.getInstance("SHA-256")
    .digest(value.toByteArray()).joinToString("") { "%02x".format(it) }
