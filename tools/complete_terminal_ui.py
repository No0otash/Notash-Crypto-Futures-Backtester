from pathlib import Path
import subprocess

SOURCE = Path('app/src/main/java/com/notash/cryptobacktester/ui/ProfessionalTerminal.kt')


def replace_once(text: str, old: str, new: str, label: str) -> str:
    count = text.count(old)
    if count == 0:
        raise SystemExit(f'{label}: anchor not found')
    if count > 1:
        raise SystemExit(f'{label}: anchor matched {count} times')
    return text.replace(old, new, 1)


def main() -> None:
    s = SOURCE.read_text(encoding='utf-8')
    original = s

    if 'private enum class TerminalLayoutMode' not in s:
        marker = 'private data class Quote(val symbol: String, val price: Double, val change: Double, val volume: Double)\n'
        helper = '''\nprivate enum class TerminalLayoutMode { COMPACT, STANDARD, EXPANDED }\n\ninternal fun terminalLayoutMode(widthDp: Int): TerminalLayoutMode = when {\n    widthDp < 600 -> TerminalLayoutMode.COMPACT\n    widthDp < 840 -> TerminalLayoutMode.STANDARD\n    else -> TerminalLayoutMode.EXPANDED\n}\n'''
        s = replace_once(s, marker, marker + helper, 'responsive layout helper')

    old_scaffold = '''Scaffold(containerColor = Bg, topBar = { Header(fa, page, { page = TerminalPage.AI }, { settings = true }, { fa = !fa }) }, bottomBar = { TerminalNavigation(page, { page = it }, fa) }) { padding ->\n            Box(Modifier.fillMaxSize().padding(padding)) {'''
    new_scaffold = '''Scaffold(containerColor = Bg, topBar = { Header(fa, page, { page = TerminalPage.AI }, { settings = true }, { fa = !fa }) }, bottomBar = { TerminalNavigation(page, { page = it }, fa) }) { padding ->\n            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {\n                Box(Modifier.fillMaxWidth().widthIn(max = 720.dp)) {'''
    if old_scaffold in s:
        s = s.replace(old_scaffold, new_scaffold, 1)
        closing = '''                }\n            }\n        }\n    }\n}'''
        if closing not in s:
            raise SystemExit('responsive container closing anchor not found')
        # Add one closing brace for the extra responsive Box, immediately before the existing Box close.
        s = s.replace(closing, '''                }\n                }\n            }\n        }\n    }\n}''', 1)

    chart_start = s.find('@Composable private fun CandleChart(')
    chart_end = s.find('@Composable private fun CandleInfo', chart_start)
    if chart_start < 0 or chart_end < 0:
        raise SystemExit('chart anchors not found')

    chart = '''@Composable private fun CandleChart(candles: List<Candle>, trades: List<TradeResult>, selected: Int?, onSelect: (Int) -> Unit, modifier: Modifier) {\n    if (candles.isEmpty()) {\n        Box(modifier.background(Panel2, RoundedCornerShape(18.dp)), contentAlignment = Alignment.Center) {\n            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(5.dp)) {\n                Text("Loading CoinEx market data…", color = PrimaryText, fontWeight = FontWeight.Bold)\n                Text("OHLC / trade diagnostics", color = Muted, fontSize = 9.sp)\n            }\n        }\n        return\n    }\n    val renderCandles = aggregateCandlesForChart(candles, 600)\n    val points = trades.mapNotNull { runCatching { buildTradingChartPoint(renderCandles, it) }.getOrNull() }\n    Canvas(modifier.background(Panel2, RoundedCornerShape(18.dp)).pointerInput(renderCandles) {\n        detectTapGestures { point -> onSelect(((point.x / size.width) * renderCandles.size).toInt().coerceIn(0, renderCandles.lastIndex)) }\n    }) {\n        val values = buildList {\n            addAll(renderCandles.flatMap { listOf(it.low, it.high) })\n            points.forEach { add(it.entryPrice); add(it.exitPrice); if (it.stopLoss > 0) add(it.stopLoss); if (it.takeProfit > 0) add(it.takeProfit) }\n        }\n        val minPrice = values.minOrNull() ?: 0.0\n        val maxPrice = values.maxOrNull() ?: 1.0\n        val range = (maxPrice - minPrice).takeIf { it > 0 } ?: 1.0\n        val step = size.width / renderCandles.size\n        val bodyWidth = (step * 0.58f).coerceAtLeast(2f)\n        fun y(price: Double): Float = size.height - ((price - minPrice) / range * size.height).toFloat()\n\n        for (g in 1..4) {\n            val gy = size.height * g / 5f\n            drawLine(Divider, Offset(0f, gy), Offset(size.width, gy), 1f)\n        }\n\n        renderCandles.forEachIndexed { index, candle ->\n            val x = index * step + step / 2f\n            val candleColor = if (candle.close >= candle.open) Green else Red\n            val top = y(maxOf(candle.open, candle.close))\n            val bottom = y(minOf(candle.open, candle.close))\n            drawLine(candleColor, Offset(x, y(candle.high)), Offset(x, y(candle.low)), 1.5f)\n            drawRect(candleColor, Offset(x - bodyWidth / 2f, top), androidx.compose.ui.geometry.Size(bodyWidth, maxOf(2f, bottom - top)))\n            if (selected == index) {\n                drawLine(PrimaryText, Offset(x, 0f), Offset(x, size.height), 1.5f)\n                drawLine(PrimaryText, Offset(0f, y(candle.close)), Offset(size.width, y(candle.close)), 1f)\n            }\n        }\n\n        points.forEach { point ->\n            val entryX = point.entryIndex * step + step / 2f\n            val exitX = point.exitIndex * step + step / 2f\n            val sideColor = if (point.side == com.notash.cryptobacktester.core.Side.LONG) Green else Red\n            drawLine(sideColor, Offset(entryX, y(point.entryPrice)), Offset(exitX, y(point.exitPrice)), 2.5f)\n            if (point.stopLoss > 0) drawLine(Red.copy(alpha = .78f), Offset(entryX, y(point.stopLoss)), Offset(exitX, y(point.stopLoss)), 1.5f)\n            if (point.takeProfit > 0) drawLine(Gold.copy(alpha = .92f), Offset(entryX, y(point.takeProfit)), Offset(exitX, y(point.takeProfit)), 1.5f)\n\n            val entryY = y(point.entryPrice)\n            if (point.side == com.notash.cryptobacktester.core.Side.LONG) {\n                drawLine(Green, Offset(entryX, entryY + 10f), Offset(entryX, entryY - 2f), 3f)\n                drawLine(Green, Offset(entryX, entryY - 2f), Offset(entryX - 5f, entryY + 3f), 3f)\n                drawLine(Green, Offset(entryX, entryY - 2f), Offset(entryX + 5f, entryY + 3f), 3f)\n            } else {\n                drawLine(Red, Offset(entryX, entryY - 10f), Offset(entryX, entryY + 2f), 3f)\n                drawLine(Red, Offset(entryX, entryY + 2f), Offset(entryX - 5f, entryY - 3f), 3f)\n                drawLine(Red, Offset(entryX, entryY + 2f), Offset(entryX + 5f, entryY - 3f), 3f)\n            }\n            drawCircle(sideColor, 7f, Offset(entryX, entryY), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f))\n            val exitY = y(point.exitPrice)\n            drawCircle(PrimaryText, 7f, Offset(exitX, exitY), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f))\n            drawCircle(sideColor, 3f, Offset(exitX, exitY))\n        }\n    }\n    Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {\n        Text("● LONG", color = Green, fontSize = 9.sp, fontWeight = FontWeight.Bold)\n        Text("● SHORT", color = Red, fontSize = 9.sp, fontWeight = FontWeight.Bold)\n        Text("○ ENTRY / EXIT", color = PrimaryText, fontSize = 9.sp)\n        Text("SL", color = Red, fontSize = 9.sp)\n        Text("TP", color = Gold, fontSize = 9.sp)\n        Spacer(Modifier.weight(1f))\n        Text("${points.size} trades", color = Muted, fontSize = 9.sp)\n    }\n}\n\n'''
    s = s[:chart_start] + chart + s[chart_end:]

    old_load = 'fun loadChart() { scope.launch { candles = runCatching { repo.loadKlines(market, timeframe, 240) }.getOrDefault(emptyList()) } }'
    new_load = 'fun loadChart() { scope.launch { val end = System.currentTimeMillis(); val start = end - 30L * 24L * 60L * 60L * 1000L; val history = com.notash.cryptobacktester.data.HistoricalDataManager(); candles = runCatching { history.downloadKlines(market, timeframe, start, end) }.getOrDefault(emptyList()) } }'
    if old_load in s:
        s = s.replace(old_load, new_load, 1)

    if s != original:
        SOURCE.write_text(s, encoding='utf-8')
        subprocess.run(['git', 'diff', '--check'], check=True)
        subprocess.run(['git', 'config', 'user.name', 'github-actions[bot]'], check=True)
        subprocess.run(['git', 'config', 'user.email', '41898282+github-actions[bot]@users.noreply.github.com'], check=True)
        subprocess.run(['git', 'add', str(SOURCE)], check=True)
        subprocess.run(['git', 'commit', '-m', 'feat: complete responsive terminal and professional chart'], check=True)
        subprocess.run(['git', 'push', 'origin', 'main'], check=True)
    else:
        print('ALVEX terminal source already complete')


if __name__ == '__main__':
    main()
