package com.notash.cryptobacktester.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val AlvexBg = Color(0xFF070A10)
private val AlvexPanel = Color(0xFF101722)
private val AlvexAccent = Color(0xFF12C8B5)
private val AlvexBlue = Color(0xFF4D7CFF)
private val AlvexMuted = Color(0xFF8B96A8)

@Composable
fun LoginGate() {
    var loggedIn by rememberSaveable { mutableStateOf(false) }
    if (loggedIn) ProfessionalTerminal() else AlvexLoginScreen { loggedIn = true }
}

@Composable
private fun AlvexLoginScreen(onLogin: () -> Unit) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var showPassword by rememberSaveable { mutableStateOf(false) }
    var rememberMe by rememberSaveable { mutableStateOf(true) }
    val valid = email.isNotBlank() && password.isNotBlank()
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF0B1020), AlvexBg, Color(0xFF05070B)))).padding(22.dp)) {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(55.dp))
            AlvexLogo(86)
            Spacer(Modifier.height(16.dp))
            Text("ALVEX", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Black, letterSpacing = 4.sp)
            Text("Crypto Intelligence • Backtesting • Market Radar", color = AlvexMuted, fontSize = 12.sp)
            Spacer(Modifier.height(30.dp))
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = AlvexPanel.copy(alpha = .96f)), elevation = CardDefaults.cardElevation(18.dp)) {
                Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("ورود به فضای کاری", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text("حساب خود را برای دسترسی به ترمینال ALVEX وارد کنید", color = AlvexMuted, fontSize = 13.sp)
                    OutlinedTextField(email, { value -> email = value }, Modifier.fillMaxWidth(), singleLine = true, label = { Text("Email / Username") }, shape = RoundedCornerShape(16.dp))
                    OutlinedTextField(
                        password, { value -> password = value }, Modifier.fillMaxWidth(), singleLine = true, label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, null) },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = { IconButton(onClick = { showPassword = !showPassword }) { Icon(if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility, null) } },
                        shape = RoundedCornerShape(16.dp)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = rememberMe, onCheckedChange = { value -> rememberMe = value })
                        Text("مرا به خاطر بسپار", color = AlvexMuted, fontSize = 12.sp)
                        Spacer(Modifier.weight(1f))
                        Text("فراموشی رمز", color = AlvexAccent, fontSize = 12.sp)
                    }
                    Button(onClick = onLogin, enabled = valid, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = AlvexAccent)) {
                        Text("ورود به ALVEX", color = Color(0xFF06100F), fontWeight = FontWeight.ExtraBold)
                    }
                    OutlinedButton(onClick = onLogin, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(16.dp)) { Text("ورود آزمایشی / Demo", color = Color.White) }
                }
            }
            Spacer(Modifier.weight(1f))
            Text("SECURE WORKSPACE  •  MARKET DATA  •  STRATEGY LAB", color = Color(0xFF647083), fontSize = 9.sp, letterSpacing = 1.2.sp)
            Spacer(Modifier.height(14.dp))
        }
    }
}

@Composable
fun AlvexLogo(size: Int) {
    Box(Modifier.size(size.dp).clip(RoundedCornerShape((size / 4).dp)).background(Brush.linearGradient(listOf(AlvexAccent, AlvexBlue))), contentAlignment = Alignment.Center) {
        Text("A", color = Color.White, fontSize = (size / 2.2).sp, fontWeight = FontWeight.Black)
    }
}
