package com.notash.cryptobacktester.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        setContent {

            MaterialTheme {

                BacktesterHome()
            }
        }
    }
}

@Composable
fun BacktesterHome() {

    var selectedMarket by remember {
        mutableStateOf("BTCUSDT")
    }

    var selectedPeriod by remember {
        mutableStateOf("1h")
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(24.dp),

        verticalArrangement =
            Arrangement.spacedBy(20.dp)
    ) {

        Text(
            text =
                "NOTASH CRYPTO BACKTESTER",

            style =
                MaterialTheme
                    .typography
                    .headlineMedium
        )

        Text(
            text =
                "Crypto Futures Strategy Lab"
        )

        Text(
            text =
                "Market: $selectedMarket"
        )

        Text(
            text =
                "Timeframe: $selectedPeriod"
        )

        Button(
            onClick = {
                selectedMarket =
                    if (
                        selectedMarket ==
                        "BTCUSDT"
                    ) {
                        "ETHUSDT"
                    } else {
                        "BTCUSDT"
                    }
            }
        ) {

            Text("Change Market")
        }

        Button(
            onClick = {
                selectedPeriod =
                    if (
                        selectedPeriod ==
                        "1h"
                    ) {
                        "15min"
                    } else {
                        "1h"
                    }
            }
        ) {

            Text("Change Timeframe")
        }

        Button(
            onClick = {
                // Backtest action will be connected next.
            }
        ) {

            Text("RUN BACKTEST")
        }
    }
}
