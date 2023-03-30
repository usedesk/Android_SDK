package ru.usedesk.knowledgebase_gui.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

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
    enabled: Boolean = true,
    radius: Dp = 30.dp,
    onClick: () -> Unit
) = focusable(true)
    .clickable(
        enabled = enabled,
        interactionSource = remember { MutableInteractionSource() },
        indication = rememberRipple(
            bounded = false,
            radius = radius
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