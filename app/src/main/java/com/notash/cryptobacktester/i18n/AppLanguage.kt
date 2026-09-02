package com.notash.cryptobacktester.i18n

/** ALVEX supported product languages. English is the canonical source language. */
enum class AppLanguage(val tag: String, val nativeName: String) {
    ENGLISH("en", "English"),
    PERSIAN("fa", "فارسی"),
    ARABIC("ar", "العربية"),
    FRENCH("fr", "Français"),
    CHINESE("zh", "中文")
}
