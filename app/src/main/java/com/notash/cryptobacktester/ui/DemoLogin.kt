package com.notash.cryptobacktester.ui

/** Local, non-Supabase demo identity used when the user cannot or does not want to authenticate. */
object DemoLogin {
    const val isEnabled: Boolean = true
    const val isLocalOnly: Boolean = true
    const val requiresSupabase: Boolean = false

    data class Session(
        val email: String,
        val isDemo: Boolean
    )

    fun createSession(): Session = Session(
        email = "demo@alvex.local",
        isDemo = true
    )
}
