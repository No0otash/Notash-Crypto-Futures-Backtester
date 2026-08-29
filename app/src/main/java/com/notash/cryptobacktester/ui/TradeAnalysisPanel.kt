package com.notash.cryptobacktester.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.notash.cryptobacktester.analysis.TradeAnalysis

@Composable
fun TradeAnalysisPanel(analysis: TradeAnalysis, fa: Boolean) {
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(if (fa) "تحلیل‌گر معاملات" else "Trade Analyst", color = Color.White, fontWeight = FontWeight.Bold)
                Text("${analysis.score}/100", color = Color(0xFF22D3EE), fontWeight = FontWeight.Black)
            }
            AnalysisSection(if (fa) "نقاط قوت" else "Strengths", analysis.strengths)
            AnalysisSection(if (fa) "نقاط ضعف" else "Weaknesses", analysis.weaknesses)
            AnalysisSection(if (fa) "پیشنهادها" else "Recommendations", analysis.recommendations)
        }
    }
}

@Composable
private fun AnalysisSection(title: String, items: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, color = Color(0xFF94A3B8), fontWeight = FontWeight.SemiBold)
        if (items.isEmpty()) Text("—", color = Color(0xFF64748B))
        else items.forEach { Text("• $it", color = Color.White) }
    }
}
