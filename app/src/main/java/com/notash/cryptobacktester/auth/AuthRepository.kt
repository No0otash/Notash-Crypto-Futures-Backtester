package com.notash.cryptobacktester.auth

/**
 * Backend contract for production authentication.
 * Implement these operations with a server/Firebase/Supabase provider.
 * No email provider secrets belong in the Android APK.
 */
interface AuthRepository {
    suspend fun requestOtp(request: OtpRequest): Result<OtpSession>
    suspend fun verifyOtp(email: String, code: String): Result<AuthUser>
    suspend fun restoreSession(): AuthUser?
    suspend fun signOut()
}
