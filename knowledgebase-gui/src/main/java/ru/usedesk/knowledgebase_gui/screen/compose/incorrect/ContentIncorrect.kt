
package ru.usedesk.knowledgebase_gui.screen.compose.incorrect

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import ru.usedesk.knowledgebase_gui.compose.ScreenNotLoaded
import ru.usedesk.knowledgebase_gui.screen.UsedeskKnowledgeBaseTheme

@Composable
internal fun ContentIncorrect(
    theme: UsedeskKnowledgeBaseTheme
) {
    Box(modifier = Modifier.fillMaxSize()) {
        ScreenNotLoaded(
            theme = theme,
            tryAgain = remember { {} },
            tryAgainVisible = null
        )
    }
}