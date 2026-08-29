package com.notash.cryptobacktester.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notash.cryptobacktester.ai.AiHubEngine

@Composable
fun AiHubEntryScreen(
    onBack: () -> Unit,
    bg: Color,
    panel: Color,
    cyan: Color
) {
    var question by remember { mutableStateOf("") }
    var answer by remember { mutableStateOf(AiHubEngine.Answer("AI Hub", "سؤال خود را درباره کریپتو، بک‌تست، ریسک یا استراتژی وارد کنید.")) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("HANNAH // AI HUB", color = cyan, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                TextButton(onClick = onBack) { Text("BACK") }
            }
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = panel)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("INDEPENDENT AI ASSISTANT", color = Color.White, fontWeight = FontWeight.Bold)
                    Text("Ask questions about crypto, trading, backtesting, risk and strategy design.", color = Color.Gray)
                    OutlinedTextField(
                        value = question,
                        onValueChange = { question = it },
                        label = { Text("Your question") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4
                    )
                    Button(
                        onClick = { answer = AiHubEngine.answer(question) },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("ASK HANNAH") }
                }
            }
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = panel)) {
                Column(Modifier.padding(16.dp)) {
                    Text(answer.title, color = cyan, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text(answer.body, color = Color.White)
                    Spacer(Modifier.height(8.dp))
                    Text("Confidence: ${answer.confidence}", color = Color.Gray, fontSize = 12.sp)
                }
            }
        }
    }
}
