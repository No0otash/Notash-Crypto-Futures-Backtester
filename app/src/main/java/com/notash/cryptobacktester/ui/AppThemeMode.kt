package com.notash.cryptobacktester.ui

enum class AppThemeMode(val storedValue: String) {
    DARK("dark"),
    LIGHT("light");

    companion object {
        fun fromStored(value: String?): AppThemeMode =
            entries.firstOrNull { it.storedValue == value } ?: DARK
    }
}

/** WCAG-style relative contrast for packed ARGB colors. */
fun relativeContrast(foregroundArgb: Long, backgroundArgb: Long): Double {
    fun channel(argb: Long, shift: Int): Double {
        val raw = ((argb shr shift) and 0xFF).toDouble() / 255.0
        return if (raw <= 0.03928) raw / 12.92 else Math.pow((raw + 0.055) / 1.055, 2.4)
    }
    fun luminance(argb: Long): Double =
        0.2126 * channel(argb, 16) + 0.7152 * channel(argb, 8) + 0.0722 * channel(argb, 0)

    val a = luminance(foregroundArgb)
    val b = luminance(backgroundArgb)
    val lighter = maxOf(a, b)
    val darker = minOf(a, b)
    return (lighter + 0.05) / (darker + 0.05)
}
