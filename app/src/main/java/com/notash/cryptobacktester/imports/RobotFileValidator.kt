package com.notash.cryptobacktester.imports

import java.util.Locale

data class RobotFileCheck(val accepted: Boolean, val extension: String, val reason: String)

object RobotFileValidator {
    private val allowed = setOf("json", "txt", "csv", "pine", "py", "kt")

    fun validate(fileName: String, sizeBytes: Long): RobotFileCheck {
        val ext = fileName.substringAfterLast('.', "").lowercase(Locale.ROOT)
        if (ext !in allowed) return RobotFileCheck(false, ext, "Unsupported strategy file format")
        if (sizeBytes <= 0L) return RobotFileCheck(false, ext, "Empty strategy file")
        if (sizeBytes > 5L * 1024L * 1024L) return RobotFileCheck(false, ext, "Strategy file exceeds 5 MB limit")
        return RobotFileCheck(true, ext, "File accepted for validation")
    }
}
