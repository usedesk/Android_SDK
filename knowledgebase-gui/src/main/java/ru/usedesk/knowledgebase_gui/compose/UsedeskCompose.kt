package ru.usedesk.knowledgebase_gui.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.usedesk.knowledgebase_gui.R

var knowledgeBaseTheme: KnowledgeBaseTheme = KnowledgeBaseTheme() //TODO

data class KnowledgeBaseTheme(
    val textStyles: TextStyles = TextStyles()
) {
    open class TextStyles {
        @Composable
        fun screenTitle() = TextStyle(
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = colorResource(R.color.usedesk_black_2)
        )
    }
}

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