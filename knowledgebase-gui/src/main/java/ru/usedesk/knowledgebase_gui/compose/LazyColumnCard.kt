package ru.usedesk.knowledgebase_gui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import ru.usedesk.knowledgebase_gui.screen.UsedeskKnowledgeBaseTheme

@Composable
internal fun Modifier.cardItem(
    theme: UsedeskKnowledgeBaseTheme,
    isTop: Boolean,
    isBottom: Boolean
): Modifier {
    val modifier = padding(bottom = if (isBottom) 16.dp else 0.dp)
    return when {
        isTop || isBottom -> modifier.clip(
            RoundedCornerShape(
                topStart = if (isTop) 10.dp else 0.dp,
                topEnd = if (isTop) 10.dp else 0.dp,
                bottomStart = if (isBottom) 10.dp else 0.dp,
                bottomEnd = if (isBottom) 10.dp else 0.dp
            )
        )
        else -> modifier
    }.background(color = theme.colors.white1)
}

@Composable
internal fun Modifier.card(theme: UsedeskKnowledgeBaseTheme) = fillMaxWidth()
    .clip(RoundedCornerShape(10.dp))
    .shadow(4.dp)
    .background(color = theme.colors.white1)