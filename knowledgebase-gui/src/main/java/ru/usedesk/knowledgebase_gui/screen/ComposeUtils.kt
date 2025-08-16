package ru.usedesk.knowledgebase_gui.screen

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

internal object ComposeUtils {
    @Composable
    fun Modifier.insetsStatusBar(theme: UsedeskKnowledgeBaseTheme) = when {
        theme.supportWindowInsets -> windowInsetsPadding(
            WindowInsets.statusBars.only(WindowInsetsSides.Top)
        )
        else -> this
    }

    @Composable
    fun Modifier.insetsBottom(theme: UsedeskKnowledgeBaseTheme) =
        windowInsetsPadding(windowInsetsBottom(theme))

    @Composable
    fun contentInsetsBottom(theme: UsedeskKnowledgeBaseTheme) = windowInsetsBottom(theme)
        .asPaddingValues()

    @Composable
    private fun windowInsetsBottom(theme: UsedeskKnowledgeBaseTheme) = when {
        theme.supportWindowInsets -> WindowInsets.navigationBars
            .only(WindowInsetsSides.Bottom)
            .union(WindowInsets.ime)
        else -> WindowInsets(0, 0, 0, 0)
    }
}