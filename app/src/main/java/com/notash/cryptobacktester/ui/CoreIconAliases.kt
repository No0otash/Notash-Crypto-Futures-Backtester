package com.notash.cryptobacktester.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Compatibility aliases for semantic icons that are not present in the
 * lightweight core Material icon artifact used by Alvex.
 *
 * Keeping these aliases local avoids pulling the extended icon dependency
 * back into the application just to satisfy individual UI glyphs.
 */
val Icons.Outlined.AutoAwesome: ImageVector get() = Icons.Outlined.Star
val Icons.Outlined.ShowChart: ImageVector get() = Icons.Outlined.Timeline
val Icons.Outlined.Science: ImageVector get() = Icons.Outlined.Build
val Icons.Outlined.AutoGraph: ImageVector get() = Icons.Outlined.Timeline
val Icons.Outlined.Psychology: ImageVector get() = Icons.Outlined.Lightbulb
val Icons.Outlined.Save: ImageVector get() = Icons.Outlined.Check
val Icons.Outlined.UploadFile: ImageVector get() = Icons.Outlined.FileUpload
val Icons.Outlined.Bolt: ImageVector get() = Icons.Outlined.Star
val Icons.Outlined.Whatshot: ImageVector get() = Icons.Outlined.TrendingUp
val Icons.Outlined.AccountBalanceWallet: ImageVector get() = Icons.Outlined.AccountBox
val Icons.Outlined.Token: ImageVector get() = Icons.Outlined.Label
val Icons.Outlined.Groups: ImageVector get() = Icons.Outlined.Person
val Icons.Outlined.Security: ImageVector get() = Icons.Outlined.Lock
val Icons.Outlined.Language: ImageVector get() = Icons.Outlined.Public
val Icons.Outlined.SupportAgent: ImageVector get() = Icons.Outlined.Person
val Icons.Outlined.Visibility: ImageVector get() = Icons.Outlined.Check
val Icons.Outlined.VisibilityOff: ImageVector get() = Icons.Outlined.Close
