package com.notash.cryptobacktester.auth

import java.util.Locale

/** Local auth state. The actual OTP must be generated and delivered by a trusted backend. */
data class AuthUser(val email: String, val verified: Boolean = true)

data class OtpRequest(val email: String, val captchaAnswer: String, val captchaToken: String)

data class OtpSession(val email: String, val expiresAtEpochMs: Long, val maskedDestination: String)

object AuthValidation {
    private val emailRegex = Regex("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", RegexOption.IGNORE_CASE)

    fun normalizeEmail(email: String): String = email.trim().lowercase(Locale.ROOT)

    fun isValidEmail(email: String): Boolean = emailRegex.matches(normalizeEmail(email))

    fun isValidCaptcha(answer: String, expected: String): Boolean =
        answer.trim().equals(expected.trim(), ignoreCase = true)

    fun isOtpFormatValid(code: String): Boolean = code.trim().matches(Regex("^\\d{6}$"))
}
