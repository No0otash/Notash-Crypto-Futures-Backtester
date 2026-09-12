package com.notash.cryptobacktester.ui

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DemoLoginTest {
    @Test
    fun demo_login_is_enabled_and_is_local_only() {
        assertTrue(DemoLogin.isEnabled)
        assertTrue(DemoLogin.isLocalOnly)
        assertFalse(DemoLogin.requiresSupabase)
    }

    @Test
    fun demo_login_creates_a_distinct_demo_session() {
        val session = DemoLogin.createSession()
        assertTrue(session.isDemo)
        assertTrue(session.email.isNotBlank())
    }
}
