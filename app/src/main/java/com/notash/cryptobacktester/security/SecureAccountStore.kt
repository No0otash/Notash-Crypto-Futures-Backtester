package com.notash.cryptobacktester.security

import android.content.Context
import java.security.MessageDigest
import java.security.SecureRandom

/** Local account state. Passwords are never persisted; only a salted SHA-256 verifier is stored. */
class SecureAccountStore(context: Context) {
    private val prefs = context.getSharedPreferences("alvex_account", Context.MODE_PRIVATE)

    fun saveEmail(email: String) = prefs.edit().putString("email", email.trim()).apply()
    fun email(): String = prefs.getString("email", "").orEmpty()

    fun savePassword(password: String) {
        require(password.length >= 8) { "Password must contain at least 8 characters" }
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        prefs.edit()
            .putString("password_salt", salt.toHex())
            .putString("password_hash", digest(password, salt))
            .apply()
    }

    fun verifyPassword(password: String): Boolean {
        val salt = prefs.getString("password_salt", null)?.hexToBytes() ?: return false
        val expected = prefs.getString("password_hash", null) ?: return false
        return MessageDigest.isEqual(digest(password, salt).toByteArray(), expected.toByteArray())
    }

    fun clear() = prefs.edit().clear().apply()

    private fun digest(value: String, salt: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(salt + value.toByteArray()).toHex()

    private fun ByteArray.toHex() = joinToString("") { "%02x".format(it) }
    private fun String.hexToBytes(): ByteArray = ByteArray(length / 2) { substring(it * 2, it * 2 + 2).toInt(16).toByte() }
}
