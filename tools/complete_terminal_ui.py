from pathlib import Path
import subprocess

SOURCE = Path('app/src/main/java/com/notash/cryptobacktester/ui/ProfessionalTerminal.kt')


def main() -> None:
    s = SOURCE.read_text(encoding='utf-8')
    fixed = s.replace(
        'private enum class TerminalLayoutMode { COMPACT, STANDARD, EXPANDED }',
        'internal enum class TerminalLayoutMode { COMPACT, STANDARD, EXPANDED }',
        1,
    )

    if fixed == s:
        print('ALVEX terminal source already normalized')
        return

    SOURCE.write_text(fixed, encoding='utf-8')
    subprocess.run(['git', 'diff', '--check'], check=True)
    subprocess.run(['git', 'config', 'user.name', 'github-actions[bot]'], check=True)
    subprocess.run(['git', 'config', 'user.email', '41898282+github-actions[bot]@users.noreply.github.com'], check=True)
    subprocess.run(['git', 'add', str(SOURCE)], check=True)
    subprocess.run(['git', 'commit', '-m', 'fix: normalize terminal layout visibility'], check=True)
    subprocess.run(['git', 'push', 'origin', 'main'], check=True)
    print('ALVEX terminal visibility normalized')


if __name__ == '__main__':
    main()
