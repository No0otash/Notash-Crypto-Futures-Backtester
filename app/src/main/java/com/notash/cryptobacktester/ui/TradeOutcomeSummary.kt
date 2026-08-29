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
import androidx.compose.ui.unit.sp
import com.notash.cryptobacktester.core.Side
import com.notash.cryptobacktester.core.TradeResult

@Composable
fun TradeOutcomeSummary(
    trades: List<TradeResult>,
    fa: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(7.dp)) {
        trades.forEachIndexed { index, trade ->
            val side = if (trade.side == Side.LONG) "LONG" else "SHORT"
            val pnlColor = if (trade.netPnl >= 0.0) Color(0xFF20E6A5) else Color(0xFFFF5577)
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF121A2D))) {
                Column(Modifier.padding(10.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("#${index + 1} • $side", color = if (trade.side == Side.LONG) Color(0xFF20E6A5) else Color(0xFFFF5577), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        Text("${if (fa) "سود خالص" else "NET PNL"}: ${"%.4f".format(trade.netPnl)}", color = pnlColor, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                    Text("Entry ${"%.4f".format(trade.entryPrice)}  →  Exit ${"%.4f".format(trade.exitPrice)}", color = Color.LightGray, fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp))
                    Text("Fees ${"%.4f".format(trade.fees)} • Funding ${"%.4f".format(trade.funding)}", color = Color.Gray, fontSize = 9.sp, modifier = Modifier.padding(top = 2.dp))
                }
            }
        }
    }
}
