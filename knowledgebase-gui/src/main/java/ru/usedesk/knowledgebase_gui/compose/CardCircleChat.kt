package ru.usedesk.knowledgebase_gui.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import ru.usedesk.knowledgebase_gui.screen.UsedeskKnowledgeBaseTheme

@Composable
internal fun BoxScope.CardCircleChat(
    theme: UsedeskKnowledgeBaseTheme,
    visible: Boolean,
    onClicked: () -> Unit
) {
    AnimatedVisibility(
        modifier = Modifier
            .align(Alignment.BottomEnd),
        visible = visible,
        enter = slideInHorizontally { it },
        exit = slideOutHorizontally { it }
    ) {
        Surface(
            shape = CircleShape,
            modifier = Modifier
                .padding(theme.dimensions.contentPadding)
                .clickableItem(onClick = onClicked),
            color = theme.colors.supportBackground,
            shadowElevation = theme.dimensions.shadowElevation
        ) {
            Icon(
                modifier = Modifier
                    .padding(theme.dimensions.supportIconPadding)
                    .size(theme.dimensions.supportIconSize),
                painter = painterResource(theme.drawables.iconSupport),
                contentDescription = null,
                tint = Color.Unspecified
            )
        }
    }
}