
package ru.usedesk.knowledgebase_gui.compose

import androidx.compose.animation.*
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import ru.usedesk.knowledgebase_gui.screen.UsedeskKnowledgeBaseTheme

@OptIn(ExperimentalAnimationApi::class)
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
        enter = remember { scaleIn(theme.animationSpec()) },
        exit = remember { scaleOut(theme.animationSpec()) }
    ) {
        Surface(
            shape = CircleShape,
            modifier = Modifier
                .padding(4.dp)
                .shadow(
                    elevation = theme.dimensions.shadowElevation,
                    shape = CircleShape
                )
                .size(theme.dimensions.loadingSize),
            color = theme.colors.progressBarBackground
        ) {
            Crossfade(
                targetState = onErrorClicked,
                animationSpec = remember { theme.animationSpec() }
            ) { onErrorClicked ->
                when (onErrorClicked) {
                    null -> CircularProgressIndicator(
                        modifier = Modifier
                            .padding(4.dp),
                        strokeWidth = theme.dimensions.progressBarStrokeWidth,
                        color = theme.colors.progressBarIndicator
                    )
                    else -> Icon(
                        modifier = Modifier
                            .clickableItem(onClick = onErrorClicked),
                        painter = painterResource(theme.drawables.iconSearchPaginationError),
                        contentDescription = null,
                        tint = Color.Unspecified
                    )
                }
            }
        }
    }
}