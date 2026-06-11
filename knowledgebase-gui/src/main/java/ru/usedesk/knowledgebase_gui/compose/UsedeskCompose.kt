
package ru.usedesk.knowledgebase_gui.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
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
        indication = ripple(bounded = true),
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
        indication = ripple(
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

/**
 * Reports via [onVisibleChange] whether the support button should be shown (list scrolled to the top),
 * observing the scroll via [snapshotFlow] so the screen isn't recomposed while scrolling.
 */
@Composable
internal fun SupportButtonVisibilityEffect(
    lazyListState: LazyListState,
    onVisibleChange: (Boolean) -> Unit
) {
    val currentOnVisibleChange by rememberUpdatedState(onVisibleChange)
    LaunchedEffect(lazyListState) {
        snapshotFlow {
            lazyListState.firstVisibleItemIndex == 0 && lazyListState.firstVisibleItemScrollOffset == 0
        }.collect { currentOnVisibleChange(it) }
    }
}

@Composable
internal fun Modifier.update(onUpdate: @Composable Modifier.() -> Modifier) = this.onUpdate()