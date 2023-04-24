
package ru.usedesk.knowledgebase_gui.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import ru.usedesk.knowledgebase_gui.screen.UsedeskKnowledgeBaseTheme

@Composable
internal fun BoxScope.CardCircleChat(
    theme: UsedeskKnowledgeBaseTheme,
    isSupportButtonVisible: Boolean,
    visible: Boolean,
    onClicked: () -> Unit
) {
    AnimatedVisibility(
        modifier = Modifier
            .align(Alignment.BottomEnd),
        visible = visible && isSupportButtonVisible,
        enter = remember { slideInHorizontally(theme.animationSpec()) { it } },
        exit = remember { slideOutHorizontally(theme.animationSpec()) { it } }
    ) {
        Box(
            modifier = Modifier
                .padding(theme.dimensions.rootPadding)
                .shadow(
                    elevation = theme.dimensions.shadowElevation,
                    shape = CircleShape
                )
                .clip(CircleShape)
                .background(color = theme.colors.supportBackground)
                .clickableItem(onClick = onClicked)
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