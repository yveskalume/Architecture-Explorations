package dev.yveskalume.newsapp.util

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier

fun Modifier.paddingAndConsumeWindowInsets(
    paddingValues: PaddingValues,
) = this
    .padding(paddingValues)
    .consumeWindowInsets(paddingValues)
