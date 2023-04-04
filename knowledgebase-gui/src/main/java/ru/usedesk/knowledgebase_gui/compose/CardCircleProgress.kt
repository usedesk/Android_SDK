package ru.usedesk.knowledgebase_gui.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import ru.usedesk.knowledgebase_gui.screen.UsedeskKnowledgeBaseTheme

@Composable
internal fun CardCircleProgress(
    theme: UsedeskKnowledgeBaseTheme,
    modifier: Modifier,
    loading: Boolean = true,
    onErrorClicked: (() -> Unit)? = null
) {
    val onErrorClicked = if (loading) null else onErrorClicked
    AnimatedVisibility(
        modifier = modifier,
        visible = loading || onErrorClicked != null,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Crossfade(targetState = onErrorClicked) { onErrorClicked ->
            Surface(
                shape = CircleShape,
                modifier = Modifier
                    .padding(4.dp)
                    .shadow(
                        elevation = 4.dp,
                        shape = CircleShape
                    )
                    .size(32.dp),
                color = when (onErrorClicked) {
                    null -> theme.colors.white1
                    else -> theme.colors.red
                }
            ) {
                when (onErrorClicked) {
                    null -> CircularProgressIndicator(
                        modifier = Modifier
                            .padding(4.dp),
                        strokeWidth = theme.dimensions.progressBarStrokeWidth,
                        color = theme.colors.red
                    )
                    else -> Icon(
                        modifier = Modifier
                            .clickableItem(onClick = onErrorClicked),
                        painter = painterResource(theme.drawables.iconIdSearchPaginationError),
                        contentDescription = null,
                        tint = Color.Unspecified
                    )
                }
            }
        }
    }
}