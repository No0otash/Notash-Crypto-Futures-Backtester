package com.notash.cryptobacktester.auth

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthValidationTest {
    @Test fun validEmailIsAccepted() { assertTrue(AuthValidation.isValidEmail("user@example.com")) }
    @Test fun invalidEmailIsRejected() { assertFalse(AuthValidation.isValidEmail("not-an-email")) }
    @Test fun captchaMustMatch() { assertTrue(AuthValidation.isCaptchaValid("A7K2", "A7K2")); assertFalse(AuthValidation.isCaptchaValid("A7K2", "A7K3")) }
    @Test fun otpMustBeSixDigits() { assertTrue(AuthValidation.isValidOtp("123456")); assertFalse(AuthValidation.isValidOtp("12345")); assertFalse(AuthValidation.isValidOtp("12345A")) }
}
