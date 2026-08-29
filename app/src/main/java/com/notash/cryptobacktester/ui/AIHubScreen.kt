package com.notash.cryptobacktester.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.notash.cryptobacktester.ai.AiHubEngine

/** Standalone AI learning and analysis workspace. */
@Composable
fun AIHubScreen(modifier: Modifier = Modifier) {
    var question by remember { mutableStateOf("") }
    var answer by remember { mutableStateOf<AiHubEngine.Answer?>(null) }
    val suggestions = listOf(
        "Funding rate چیست؟",
        "Long و Short چه تفاوتی دارند؟",
        "Tokenomics را چطور بررسی کنم؟",
        "ریسک یک معامله Futures چیست؟",
        "بک‌تست خوب چه ویژگی‌هایی دارد؟"
    )

    LazyColumn(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("AI HUB", style = MaterialTheme.typography.headlineMedium)
            Text("آموزش، پرسش‌وپاسخ و تحلیل داده‌محور")
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = question,
                        onValueChange = { question = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("سؤال خود را بپرسید") }
                    )
                    Button(
                        onClick = { answer = AiHubEngine.answer(question) },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("تحلیل / پاسخ") }
                }
            }
        }
        item { Text("سؤال‌های پیشنهادی", style = MaterialTheme.typography.titleMedium) }
        items(suggestions) { suggestion ->
            Row(Modifier.fillMaxWidth()) {
                Button(onClick = { question = suggestion; answer = AiHubEngine.answer(suggestion) }) {
                    Text(suggestion)
                }
            }
        }
        answer?.let { result ->
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(result.title, style = MaterialTheme.typography.titleLarge)
                        Text(result.body)
                        Text("Data confidence: ${result.confidence}")
                    }
                }
            }
        }
    }
}
