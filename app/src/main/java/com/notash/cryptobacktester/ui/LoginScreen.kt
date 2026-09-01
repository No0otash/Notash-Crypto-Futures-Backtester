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

private val LoginBg = Color(0xFF070A10)
private val LoginPanel = Color(0xFF101722)
private val LoginAccent = Color(0xFF12C8B5)
private val LoginMuted = Color(0xFF8B96A8)

@Composable
fun LoginGate() {
    val context = LocalContext.current
    val auth = remember { SupabaseAuth(context) }
    var loggedIn by rememberSaveable { mutableStateOf(auth.currentSession() != null) }
    var showAccount by rememberSaveable { mutableStateOf(false) }
    if (!loggedIn) LoginScreen(auth) { loggedIn = true; showAccount = true }
    else if (showAccount) AccountCenterScreen(onContinue = { showAccount = false }, onSignedOut = { loggedIn = false; showAccount = false })
    else ProfessionalTerminal()
}

@Composable
private fun LoginScreen(auth: SupabaseAuth, onLogin: () -> Unit) {
    val scope = rememberCoroutineScope()
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var success by remember { mutableStateOf(false) }
    val valid = email.isNotBlank() && password.length >= 6

    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF0B1020), LoginBg, Color(0xFF05070B)))).padding(22.dp)) {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(55.dp)); AlvexLogo(86); Spacer(Modifier.height(16.dp))
            Text("ALVEX", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Black, letterSpacing = 4.sp)
            Text("Crypto Intelligence • Backtesting • Market Radar", color = LoginMuted, fontSize = 12.sp)
            Spacer(Modifier.height(30.dp))
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = LoginPanel), elevation = CardDefaults.cardElevation(18.dp)) {
                Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("ورود به فضای کاری", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text("حساب ALVEX خود را وارد کنید", color = LoginMuted, fontSize = 13.sp)
                    OutlinedTextField(email, { email = it }, Modifier.fillMaxWidth(), singleLine = true, label = { Text("Email") }, shape = RoundedCornerShape(16.dp))
                    OutlinedTextField(password, { password = it }, Modifier.fillMaxWidth(), singleLine = true, label = { Text("Password (min 6) ") }, leadingIcon = { Icon(Icons.Outlined.Lock, null) }, visualTransformation = PasswordVisualTransformation(), shape = RoundedCornerShape(16.dp))
                    message?.let { Text(it, color = if (success) LoginAccent else Color(0xFFFF6B7A), fontSize = 12.sp) }
                    Button(onClick = { loading = true; message = null; scope.launch { val r = auth.signIn(email, password); loading = false; if (r.error == null) onLogin() else message = r.error } }, enabled = valid && !loading, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = LoginAccent)) { Text(if (loading) "در حال اتصال…" else "ورود به ALVEX", color = Color(0xFF06100F), fontWeight = FontWeight.ExtraBold) }
                    OutlinedButton(onClick = { loading = true; message = null; scope.launch { val r = auth.signUp(email, password); loading = false; if (r.error == null) { success = true; if (r.value?.accessToken?.isNotBlank() == true) onLogin() else message = "حساب ساخته شد؛ ایمیل خود را برای تأیید بررسی کنید." } else message = r.error } }, enabled = valid && !loading, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(16.dp)) { Text("ساخت حساب جدید", color = Color.White) }
                    TextButton(enabled = email.isNotBlank() && !loading, onClick = { loading = true; message = null; scope.launch { val r = auth.sendPasswordReset(email); loading = false; success = r.error == null; message = r.error ?: "ایمیل بازیابی رمز ارسال شد." } }) { Text("فراموشی رمز عبور", color = LoginAccent) }
                }
            }
            Spacer(Modifier.weight(1f)); Text("SECURE WORKSPACE • SUPABASE AUTH • MARKET DATA", color = Color(0xFF647083), fontSize = 9.sp, letterSpacing = 1.2.sp); Spacer(Modifier.height(14.dp))
        }
    }
}
