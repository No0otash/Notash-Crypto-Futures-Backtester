from pathlib import Path
import subprocess

SOURCE = Path('app/src/main/java/com/notash/cryptobacktester/ui/ProfessionalTerminal.kt')


def replace_once(text: str, old: str, new: str) -> str:
    return text.replace(old, new, 1) if old in text else text


def main() -> None:
    s = SOURCE.read_text(encoding='utf-8')
    fixed = s
    fixed = replace_once(fixed, 'private enum class TerminalLayoutMode { COMPACT, STANDARD, EXPANDED }', 'internal enum class TerminalLayoutMode { COMPACT, STANDARD, EXPANDED }')
    fixed = replace_once(fixed, 'Scaffold(containerColor = Bg, topBar = { Header(fa, page, { page = TerminalPage.AI }, { settings = true }, { fa = !fa }) }, bottomBar = { TerminalNavigation(page, { page = it }, fa) }) { padding ->', 'Scaffold(contentWindowInsets = WindowInsets.safeDrawing, containerColor = Bg, topBar = { Header(fa, page, { page = TerminalPage.AI }, { settings = true }, { fa = !fa }) }, bottomBar = { TerminalNavigation(page, { page = it }, fa) }) { padding ->')
    fixed = replace_once(fixed, 'Box(Modifier.fillMaxWidth().height(68.dp).background(Bg)) {', 'Box(Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top)).height(68.dp).background(Bg)) {')
    fixed = replace_once(fixed, 'TerminalPage.INTELLIGENCE -> IntelligenceScreen(fa, market)', 'TerminalPage.INTELLIGENCE -> AlvexIntelligenceScreen(fa, market)')
    fixed = replace_once(fixed, 'val repo = remember { CoinExRepository() }; val scope = rememberCoroutineScope(); val symbols = remember { listOf("BTCUSDT", "ETHUSDT", "SOLUSDT", "XRPUSDT", "DOGEUSDT", "PEPEUSDT") }', 'val repo = remember { CoinExRepository() }; val scope = rememberCoroutineScope()')
    fixed = replace_once(fixed, 'fun refresh() { scope.launch { loading = true; quotes = withContext(Dispatchers.IO) { symbols.mapNotNull { symbol -> val ticker = runCatching { repo.loadLatestTicker(symbol) }.getOrNull() ?: return@mapNotNull null; Quote(symbol, ticker.last, ticker.changeRate * 100.0, ticker.volume) } }; loading = false } }', 'fun refresh() { scope.launch { loading = true; quotes = withContext(Dispatchers.IO) { runCatching { repo.loadFuturesTickers().sortedByDescending { it.quoteVolume24h }.take(12).map { Quote(it.market, it.lastPrice, if (it.open24h > 0.0) (it.lastPrice / it.open24h - 1.0) * 100.0 else 0.0, it.quoteVolume24h) } }.getOrDefault(emptyList()) }; loading = false } }')

    if fixed == s:
        print('ALVEX terminal source already normalized')
        return
    SOURCE.write_text(fixed, encoding='utf-8')
    subprocess.run(['git', 'diff', '--check'], check=True)
    subprocess.run(['git', 'config', 'user.name', 'github-actions[bot]'], check=True)
    subprocess.run(['git', 'config', 'user.email', '41898282+github-actions[bot]@users.noreply.github.com'], check=True)
    subprocess.run(['git', 'add', str(SOURCE)], check=True)
    subprocess.run(['git', 'commit', '-m', 'feat: connect ALVEX terminal to live intelligence'], check=True)
    subprocess.run(['git', 'push', 'origin', 'main'], check=True)
    print('ALVEX terminal intelligence wiring applied')


if __name__ == '__main__':
    main()
