
package ru.usedesk.knowledgebase_gui.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import ru.usedesk.knowledgebase_gui.screen.UsedeskKnowledgeBaseTheme

@Composable
internal fun Modifier.clickableItem(
    enabled: Boolean = true,
    onClick: () -> Unit
) = focusable(true)
    .clickable(
        enabled = enabled,
        interactionSource = remember { MutableInteractionSource() },
        indication = rememberRipple(bounded = true),
        onClick = onClick
    )

@Composable
internal fun Modifier.clickableArea(
    theme: UsedeskKnowledgeBaseTheme,
    enabled: Boolean = true,
    onClick: () -> Unit
) = focusable(true)
    .clickable(
        enabled = enabled,
        interactionSource = remember { MutableInteractionSource() },
        indication = rememberRipple(
            bounded = false,
            radius = theme.dimensions.clickableRadius
        ),
        onClick = onClick
    )

@Composable
internal fun Modifier.clickableText(
    enabled: Boolean = true,
    onClick: () -> Unit
) = focusable(true)
    .clickable(
        enabled = enabled,
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick
    )

@Composable
internal fun Modifier.padding(padding: UsedeskKnowledgeBaseTheme.Dimensions.Padding) = padding(
    start = padding.start,
    end = padding.end,
    top = padding.top,
    bottom = padding.bottom
)

@Composable
internal fun LazyListState.isSupportButtonVisible() = remember(this) {
    derivedStateOf {
        firstVisibleItemIndex == 0 && firstVisibleItemScrollOffset == 0
    }
}.value

@Composable
internal fun Modifier.update(onUpdate: @Composable Modifier.() -> Modifier) = this.onUpdate()