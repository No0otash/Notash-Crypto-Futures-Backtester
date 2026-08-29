package com.notash.cryptobacktester.ui

import androidx.compose.runtime.Composable
import com.notash.cryptobacktester.ai.AiStrategyBuilderPage as Builder

@Composable
fun AiStrategyBuilderPage(
    fa: Boolean,
    onGenerate: (String) -> Unit,
    onUseForBacktest: () -> Unit,
    onSave: () -> Unit
) = Builder(fa, onGenerate, onUseForBacktest, onSave)
