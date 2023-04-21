
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
    val modifier = padding(
        start = theme.dimensions.rootPadding.start,
        end = theme.dimensions.rootPadding.end,
        bottom = if (isBottom) theme.dimensions.rootPadding.bottom else 0.dp
    )
    return when {
        isTop || isBottom -> modifier.clip(
            RoundedCornerShape(
                topStart = if (isTop) theme.dimensions.contentCornerRadius else 0.dp,
                topEnd = if (isTop) theme.dimensions.contentCornerRadius else 0.dp,
                bottomStart = if (isBottom) theme.dimensions.contentCornerRadius else 0.dp,
                bottomEnd = if (isBottom) theme.dimensions.contentCornerRadius else 0.dp
            )
        )
        else -> modifier
    }.background(color = theme.colors.listItemBackground)
}

@Composable
internal fun Modifier.card(theme: UsedeskKnowledgeBaseTheme) = fillMaxWidth()
    .clip(RoundedCornerShape(theme.dimensions.contentCornerRadius))
    .shadow(elevation = theme.dimensions.shadowElevation)
    .background(color = theme.colors.listItemBackground)