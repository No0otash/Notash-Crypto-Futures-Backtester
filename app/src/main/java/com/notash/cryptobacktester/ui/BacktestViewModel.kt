package com.notash.cryptobacktester.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        setContent {

            MaterialTheme {

                BacktesterScreen()
            }
        }
    }
}

@Composable
fun BacktesterScreen(
    vm: BacktestViewModel =
        viewModel()
) {

    val state by
        vm.state.collectAsState()

    val scroll =
        rememberScrollState()

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(20.dp),

        verticalArrangement =
            Arrangement.spacedBy(14.dp)
    ) {

        Text(
            text =
                "NOTASH CRYPTO LAB",

            style =
                MaterialTheme
                    .typography
                    .headlineMedium
        )

        Text(
            text =
                "Crypto Futures Backtester",

            style =
                MaterialTheme
                    .typography
                    .bodyLarge
        )

        Spacer(
            modifier =
                Modifier.height(8.dp)
        )

        MarketSelector(
            selected =
                state.market,

            onSelected =
                vm::setMarket
        )

        TimeframeSelector(
            selected =
                state.timeframe,

            onSelected =
                vm::setTimeframe
        )

        Card(
            modifier =
                Modifier.fillMaxWidth()
        ) {

            Column(
                modifier =
                    Modifier.padding(16.dp),

                verticalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {

                Text(
                    text =
                        "Strategy"
                )

                Text(
                    text =
                        "Advanced Pullback v1"
                )

                Text(
                    text =
                        "LWMA 20 / 50 + ATR"
                )
            }
        }

        Button(
            modifier =
                Modifier.fillMaxWidth(),

            enabled =
                !state.isRunning,

            onClick =
                vm::runBacktest
        ) {

            if (state.isRunning) {

                CircularProgressIndicator()

            } else {

                Text(
                    text =
                        "RUN BACKTEST"
                )
            }
        }

        Text(
            text =
                state.status
        )

        state.error?.let {

            Card(
                modifier =
                    Modifier.fillMaxWidth()
            ) {

                Text(
                    text =
                        "ERROR\n$it",

                    modifier =
                        Modifier.padding(16.dp)
                )
            }
        }

        state.report?.let {

            ReportCard(it)
        }
    }
}

@Composable
fun MarketSelector(
    selected: String,
    onSelected: (String) -> Unit
) {

    var expanded by
        remember {
            mutableStateOf(false)
        }

    Column {

        Text(
            text =
                "Market"
        )

        Spacer(
            modifier =
                Modifier.height(6.dp)
        )

        OutlinedButton(
            onClick = {
                expanded = true
            }
        ) {

            Text(
                text =
                    selected
            )
        }

        DropdownMenu(
            expanded =
                expanded,

            onDismissRequest = {
                expanded = false
            }
        ) {

            listOf(
                "BTCUSDT",
                "ETHUSDT",
                "SOLUSDT"
            ).forEach { market ->

                DropdownMenuItem(
                    text = {
                        Text(market)
                    },

                    onClick = {

                        onSelected(market)

                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun TimeframeSelector(
    selected: String,
    onSelected: (String) -> Unit
) {

    var expanded by
        remember {
            mutableStateOf(false)
        }

    Column {

        Text(
            text =
                "Timeframe"
        )

        Spacer(
            modifier =
                Modifier.height(6.dp)
        )

        OutlinedButton(
            onClick = {
                expanded = true
            }
        ) {

            Text(
                text =
                    selected
            )
        }

        DropdownMenu(
            expanded =
                expanded,

            onDismissRequest = {
                expanded = false
            }
        ) {

            listOf(
                "1min",
                "5min",
                "15min",
                "30min",
                "1h",
                "4h",
                "1d"
            ).forEach { timeframe ->

                DropdownMenuItem(
                    text = {
                        Text(timeframe)
                    },

                    onClick = {

                        onSelected(timeframe)

                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun ReportCard(
    report:
        com.notash.cryptobacktester.core.BacktestReport
) {

    Card(
        modifier =
            Modifier.fillMaxWidth()
    ) {

        Column(
            modifier =
                Modifier.padding(16.dp),

            verticalArrangement =
                Arrangement.spacedBy(10.dp)
        ) {

            Text(
                text =
                    "BACKTEST RESULT",

                style =
                    MaterialTheme
                        .typography
                        .titleLarge
            )

            Metric(
                "Initial Balance",
                report.initialBalance
            )

            Metric(
                "Final Balance",
                report.finalBalance
            )

            Metric(
                "Net PNL",
                report.netPnl
            )

            Metric(
                "ROI",
                report.roiPercent,
                "%"
            )

            Metric(
                "Win Rate",
                report.winRatePercent,
                "%"
            )

            Metric(
                "Max Drawdown",
                report.maxDrawdownPercent,
                "%"
            )

            Metric(
                "Profit Factor",
                report.profitFactor
            )

            Metric(
                "Fees",
                report.totalFees
            )

            Metric(
                "Funding",
                report.totalFunding
            )

            Text(
                text =
                    "Trades: ${report.trades.size}"
            )
        }
    }
}

@Composable
fun Metric(
    title: String,
    value: Double,
    suffix: String = ""
) {

    Row(
        modifier =
            Modifier.fillMaxWidth(),

        horizontalArrangement =
            Arrangement.SpaceBetween
    ) {

        Text(title)

        Text(
            text =
                String.format(
                    "%.2f%s",
                    value,
                    suffix
                )
        )
    }
}
