package com.notash.cryptobacktester.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notash.cryptobacktester.auth.SupabaseAuth
import kotlinx.coroutines.launch

private val Bg = Color(0xFF070A10)
private val Panel = Color(0xFF101722)
private val Accent = Color(0xFF12C8B5)
private val Muted = Color(0xFF8995A8)

@Composable
fun AccountCenterScreen(onContinue: () -> Unit, onSignedOut: () -> Unit) {
    val context = LocalContext.current
    val auth = remember { SupabaseAuth(context) }
    val session = auth.currentSession()
    val scope = rememberCoroutineScope()
    val prefs = remember { context.getSharedPreferences("alvex_preferences", 0) }
    var email by rememberSaveable { mutableStateOf(session?.email.orEmpty()) }
    var newEmail by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var language by rememberSaveable { mutableStateOf(prefs.getString("language", "English") ?: "English") }
    var notifications by rememberSaveable { mutableStateOf(prefs.getBoolean("notifications", true)) }
    var privacy by rememberSaveable { mutableStateOf(prefs.getBoolean("privacy", true)) }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    fun savePrefs() = prefs.edit().putString("language", language).putBoolean("notifications", notifications).putBoolean("privacy", privacy).apply()

    LazyColumn(Modifier.fillMaxSize().background(Bg).padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 28.dp)) {
        item { Text("ALVEX Account", color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.Black); Text("Profile • Settings • Security", color = Muted, fontSize = 12.sp) }
        item {
            Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Panel)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Profile", color = Color.White, fontSize = 19.sp)
                    OutlinedTextField(email, {}, Modifier.fillMaxWidth(), enabled = false, label = { Text("Current email") })
                    OutlinedTextField(newEmail, { newEmail = it }, Modifier.fillMaxWidth(), singleLine = true, label = { Text("New email") })
                    Button(enabled = newEmail.isNotBlank() && !busy, onClick = {
                        busy = true; message = null
                        scope.launch { val r = auth.requestEmailChange(newEmail); busy = false; message = r.error ?: "Verification email sent."; if (r.error == null) newEmail = "" }
                    }, modifier = Modifier.fillMaxWidth()) { Text(if (busy) "Updating…" else "Change email") }
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Panel)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Security", color = Color.White, fontSize = 19.sp)
                    OutlinedTextField(newPassword, { newPassword = it }, Modifier.fillMaxWidth(), singleLine = true, label = { Text("New password (min 6)") }, visualTransformation = PasswordVisualTransformation(), leadingIcon = { Icon(Icons.Outlined.Lock, null) })
                    Button(enabled = newPassword.length >= 6 && !busy, onClick = {
                        busy = true; message = null
                        scope.launch { val r = auth.updatePassword(newPassword); busy = false; message = r.error ?: "Password updated successfully."; if (r.error == null) newPassword = "" }
                    }, modifier = Modifier.fillMaxWidth()) { Text("Change password") }
                    OutlinedButton(enabled = !busy, onClick = {
                        busy = true; message = null
                        scope.launch { val r = auth.sendPasswordReset(email); busy = false; message = r.error ?: "Password recovery email sent." }
                    }, modifier = Modifier.fillMaxWidth()) { Text("Send password reset email") }
                    OutlinedButton(enabled = !busy, onClick = {
                        busy = true
                        scope.launch { val r = auth.signOut(); busy = false; if (r.error == null) onSignedOut() else message = r.error }
                    }, modifier = Modifier.fillMaxWidth()) { Text("Sign out") }
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Panel)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Settings", color = Color.White, fontSize = 19.sp)
                    listOf("English", "فارسی", "العربية", "Français", "中文").forEach { option ->
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { RadioButton(selected = language == option, onClick = { language = option; savePrefs() }); Text(option, color = Color.White) }
                    }
                    HorizontalDivider()
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Text("Notifications", color = Color.White, modifier = Modifier.weight(1f)); Switch(checked = notifications, onCheckedChange = { notifications = it; savePrefs() }) }
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Text("Privacy mode", color = Color.White, modifier = Modifier.weight(1f)); Switch(checked = privacy, onCheckedChange = { privacy = it; savePrefs() }) }
                }
            }
        }
        item { message?.let { Text(it, color = Accent, fontSize = 12.sp) } }
        item { Button(onClick = onContinue, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Accent)) { Text("Continue to ALVEX", color = Color(0xFF06100F)) } }
    }
}