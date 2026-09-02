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

private val LoginAccent = Color(0xFF12C8B5)
private val DarkBg = Color(0xFF070A10)
private val DarkPanel = Color(0xFF101722)
private val LightBg = Color(0xFFF3F6FA)
private val LightPanel = Color(0xFFFFFFFF)

@Composable
fun LoginGate() {
    val context = LocalContext.current
    val auth = remember { SupabaseAuth(context) }
    val prefs = remember { context.getSharedPreferences("alvex_preferences", 0) }
    var themeMode by rememberSaveable { mutableStateOf(AppThemeMode.fromStored(prefs.getString("theme_mode", null))) }
    var loggedIn by rememberSaveable { mutableStateOf(auth.currentSession() != null) }
    var showAccount by rememberSaveable { mutableStateOf(false) }
    fun changeTheme(mode: AppThemeMode) { themeMode = mode; prefs.edit().putString("theme_mode", mode.storedValue).apply() }
    val dark = themeMode == AppThemeMode.DARK
    val colorScheme = if (dark) darkColorScheme(primary = LoginAccent, secondary = Color(0xFF8A45FF), background = DarkBg, surface = DarkPanel, onBackground = Color(0xFFF4F7FB), onSurface = Color(0xFFF4F7FB)) else lightColorScheme(primary = Color(0xFF007C70), secondary = Color(0xFF315CCB), background = LightBg, surface = LightPanel, onBackground = Color(0xFF18202B), onSurface = Color(0xFF18202B))
    MaterialTheme(colorScheme = colorScheme) {
        when {
            !loggedIn -> LoginScreen(auth, themeMode) { loggedIn = true; showAccount = true }
            showAccount -> AccountCenterScreen(onContinue = { showAccount = false }, onSignedOut = { loggedIn = false; showAccount = false }, themeMode = themeMode, onThemeMode = ::changeTheme)
            else -> AlvexPremiumWorkspace(themeMode = themeMode, onThemeMode = ::changeTheme)
        }
    }
}

@Composable
private fun LoginScreen(auth: SupabaseAuth, themeMode: AppThemeMode, onLogin: () -> Unit) {
    val scope = rememberCoroutineScope(); var email by rememberSaveable { mutableStateOf("") }; var password by rememberSaveable { mutableStateOf("") }; var loading by remember { mutableStateOf(false) }; var message by remember { mutableStateOf<String?>(null) }; var success by remember { mutableStateOf(false) }
    val valid = email.isNotBlank() && password.length >= 6; val dark = themeMode == AppThemeMode.DARK; val scheme = MaterialTheme.colorScheme; val muted = if (dark) Color(0xFFB8C1CF) else Color(0xFF4E5A69)
    val background = if (dark) Brush.verticalGradient(listOf(Color(0xFF0B1020), DarkBg, Color(0xFF05070B))) else Brush.verticalGradient(listOf(Color.White, LightBg, Color(0xFFE8EEF5)))
    val fieldColors = OutlinedTextFieldDefaults.colors(focusedTextColor = scheme.onSurface, unfocusedTextColor = scheme.onSurface, cursorColor = scheme.primary, focusedLabelColor = scheme.primary, unfocusedLabelColor = muted, focusedLeadingIconColor = scheme.primary, unfocusedLeadingIconColor = muted, focusedBorderColor = scheme.primary, unfocusedBorderColor = if (dark) Color(0xFF526072) else Color(0xFF7A8797), focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent)
    Box(Modifier.fillMaxSize().background(background).padding(22.dp)) {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(48.dp)); AlvexLogo(82); Spacer(Modifier.height(14.dp)); Text("ALVEX", color = scheme.onBackground, fontSize = 34.sp, fontWeight = FontWeight.Black, letterSpacing = 4.sp); Text("AI Market Intelligence", color = muted, fontSize = 12.sp); Spacer(Modifier.height(28.dp))
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = scheme.surface), elevation = CardDefaults.cardElevation(18.dp)) {
                Column(Modifier.padding(23.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
                    Text("ورود به حساب کاربری", color = scheme.onSurface, fontSize = 23.sp, fontWeight = FontWeight.Bold); Text("Crypto Intelligence • Backtesting • Market Radar", color = muted, fontSize = 11.sp)
                    OutlinedTextField(email, { email = it }, Modifier.fillMaxWidth(), singleLine = true, label = { Text("Email") }, shape = RoundedCornerShape(16.dp), colors = fieldColors)
                    OutlinedTextField(password, { password = it }, Modifier.fillMaxWidth(), singleLine = true, label = { Text("Password") }, leadingIcon = { Icon(Icons.Outlined.Lock, null) }, visualTransformation = PasswordVisualTransformation(), shape = RoundedCornerShape(16.dp), colors = fieldColors)
                    message?.let { Text(it, color = if (success) scheme.primary else Color(0xFFD72F49), fontSize = 11.sp) }
                    Button(onClick = { loading = true; message = null; scope.launch { val r = auth.signIn(email, password); loading = false; if (r.error == null) onLogin() else message = r.error } }, enabled = valid && !loading, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = scheme.primary)) { Text(if (loading) "در حال اتصال…" else "ورود به حساب کاربری", color = scheme.onPrimary, fontWeight = FontWeight.ExtraBold) }
                    OutlinedButton(onClick = { loading = true; message = null; scope.launch { val r = auth.signUp(email, password); loading = false; if (r.error == null) { success = true; if (r.value?.accessToken?.isNotBlank() == true) onLogin() else message = "حساب ساخته شد؛ ایمیل خود را برای تأیید بررسی کنید." } else message = r.error } }, enabled = valid && !loading, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(16.dp)) { Text("ثبت‌نام", color = scheme.onSurface) }
                    TextButton(enabled = email.isNotBlank() && !loading, onClick = { loading = true; message = null; scope.launch { val r = auth.sendPasswordReset(email); loading = false; success = r.error == null; message = r.error ?: "ایمیل بازیابی رمز ارسال شد." } }) { Text("فراموشی رمز عبور", color = scheme.primary) }
                }
            }
            Spacer(Modifier.weight(1f)); Text("SECURE WORKSPACE • SUPABASE AUTH • MARKET DATA", color = muted, fontSize = 8.sp, letterSpacing = 1.sp); Spacer(Modifier.height(12.dp))
        }
    }
}
