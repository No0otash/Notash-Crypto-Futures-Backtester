package com.notash.cryptobacktester.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notash.cryptobacktester.auth.SupabaseAuth
import kotlinx.coroutines.launch

private val LoginTeal = Color(0xFF12C8B5)
private val LoginPurple = Color(0xFF8A45FF)
private val LoginDarkBg = Color(0xFF070A10)
private val LoginDarkPanel = Color(0xFF101722)
private val LoginLightBg = Color(0xFFF3F6FA)

@Composable
fun LoginGate() {
    val context = LocalContext.current
    val auth = remember { SupabaseAuth(context) }
    val prefs = remember { context.getSharedPreferences("alvex_preferences", 0) }
    var mode by rememberSaveable { mutableStateOf(AppThemeMode.fromStored(prefs.getString("theme_mode", null))) }
    var logged by rememberSaveable { mutableStateOf(auth.currentSession() != null) }
    var account by rememberSaveable { mutableStateOf(false) }

    fun setTheme(value: AppThemeMode) {
        mode = value
        prefs.edit().putString("theme_mode", value.storedValue).apply()
    }

    MaterialTheme(
        colorScheme = if (mode == AppThemeMode.DARK) {
            darkColorScheme(primary = LoginTeal, secondary = LoginPurple, background = LoginDarkBg, surface = LoginDarkPanel, onBackground = Color.White, onSurface = Color.White)
        } else {
            lightColorScheme(primary = Color(0xFF007C70), secondary = Color(0xFF315CCB), background = LoginLightBg, surface = Color.White, onBackground = Color(0xFF18202B), onSurface = Color(0xFF18202B))
        }
    ) {
        when {
            !logged -> LoginForm(auth, mode) { logged = true; account = false }
            account -> AccountCenterScreen({ account = false }, { logged = false; account = false }, mode, ::setTheme)
            else -> AlvexPremiumFinal(mode, ::setTheme)
        }
    }
}

@Composable
private fun LoginForm(auth: SupabaseAuth, mode: AppThemeMode, onLogin: () -> Unit) {
    val scope = rememberCoroutineScope()
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var success by remember { mutableStateOf(false) }
    val dark = mode == AppThemeMode.DARK
    val muted = if (dark) Color(0xFFB8C1CF) else Color(0xFF4E5A69)
    val valid = email.isNotBlank() && password.length >= 6
    val background = if (dark) {
        Brush.verticalGradient(listOf(Color(0xFF0B1020), LoginDarkBg, Color(0xFF05070B)))
    } else {
        Brush.verticalGradient(listOf(Color.White, LoginLightBg, Color(0xFFE8EEF5)))
    }

    Box(Modifier.fillMaxSize().background(background).padding(22.dp)) {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(48.dp))
            Box(Modifier.size(78.dp).background(Brush.linearGradient(listOf(LoginPurple, LoginTeal)), RoundedCornerShape(24.dp)), contentAlignment = Alignment.Center) {
                Text("A", color = Color.White, fontSize = 38.sp, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.height(12.dp))
            Text("ALVEX", color = MaterialTheme.colorScheme.onBackground, fontSize = 34.sp, fontWeight = FontWeight.Black, letterSpacing = 4.sp)
            Text("AI Market Intelligence", color = muted, fontSize = 12.sp)
            Spacer(Modifier.height(28.dp))

            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 18.dp)) {
                Column(Modifier.padding(23.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
                    Text("ورود به حساب کاربری", color = MaterialTheme.colorScheme.onSurface, fontSize = 23.sp, fontWeight = FontWeight.Bold)
                    Text("Crypto Intelligence • Backtesting • Market Radar", color = muted, fontSize = 11.sp)
                    OutlinedTextField(value = email, onValueChange = { email = it }, modifier = Modifier.fillMaxWidth(), singleLine = true, label = { Text("Email") }, shape = RoundedCornerShape(16.dp))
                    OutlinedTextField(value = password, onValueChange = { password = it }, modifier = Modifier.fillMaxWidth(), singleLine = true, label = { Text("Password") }, leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) }, visualTransformation = PasswordVisualTransformation(), shape = RoundedCornerShape(16.dp))
                    message?.let { Text(it, color = if (success) MaterialTheme.colorScheme.primary else Color(0xFFD72F49), fontSize = 11.sp) }
                    Button(onClick = {
                        loading = true
                        scope.launch {
                            val result = auth.signIn(email.trim(), password)
                            loading = false
                            if (result.error == null) onLogin() else { success = false; message = result.error }
                        }
                    }, enabled = valid && !loading, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(16.dp)) {
                        Text(if (loading) "در حال اتصال…" else "ورود به حساب کاربری", fontWeight = FontWeight.ExtraBold)
                    }
                    OutlinedButton(onClick = {
                        loading = true
                        scope.launch {
                            val result = auth.signUp(email.trim(), password)
                            loading = false
                            if (result.error == null) { success = true; message = "حساب ساخته شد؛ ایمیل را بررسی کنید." } else { success = false; message = result.error }
                        }
                    }, enabled = valid && !loading, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(16.dp)) {
                        Text("ثبت‌نام")
                    }
                    TextButton(onClick = {
                        loading = true
                        scope.launch {
                            val result = auth.sendPasswordReset(email.trim())
                            loading = false
                            success = result.error == null
                            message = result.error ?: "ایمیل بازیابی رمز ارسال شد."
                        }
                    }, enabled = email.isNotBlank() && !loading) {
                        Text("فراموشی رمز عبور", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            Text("SECURE WORKSPACE • SUPABASE AUTH • MARKET DATA", color = muted, fontSize = 8.sp)
            Spacer(Modifier.height(12.dp))
        }
    }
}
