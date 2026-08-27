package com.notash.cryptobacktester.auth

object AuthValidation {
    private val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val otpRegex = Regex("^\\d{6}$")

    fun isValidEmail(email: String): Boolean = emailRegex.matches(email.trim())
    fun isCaptchaValid(expected: String, entered: String): Boolean = expected.equals(entered.trim(), ignoreCase = true)
    fun isValidOtp(otp: String): Boolean = otpRegex.matches(otp.trim())
}
