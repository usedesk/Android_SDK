package ru.usedesk.knowledgebase_gui.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.usedesk.knowledgebase_gui.screen.UsedeskKnowledgeBaseCustomization

@Composable
internal fun CardCircleProgress(
    customization: UsedeskKnowledgeBaseCustomization,
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
                    null -> colorResource(customization.colorIdWhite1)
                    else -> colorResource(customization.colorIdRed)
                }
            ) {
                when (onErrorClicked) {
                    null -> CircularProgressIndicator(
                        modifier = Modifier
                            .padding(4.dp)
                    )
                    else -> BasicText(
                        modifier = Modifier
                            .clickableItem(onClick = onErrorClicked),
                        text = "!",
                        style = TextStyle(
                            color = colorResource(customization.colorIdWhite1),
                            fontSize = 24.sp,
                            textAlign = TextAlign.Center
                        )
                    )
                }
            }
        }
    }
}