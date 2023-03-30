package ru.usedesk.knowledgebase_gui.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import ru.usedesk.knowledgebase_gui.screen.UsedeskKnowledgeBaseCustomization

@Composable
internal fun CardCircleProgress(
    customization: UsedeskKnowledgeBaseCustomization,
    modifier: Modifier,
    visible: Boolean = true
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Card(
            modifier = Modifier
                .padding(16.dp),
            shape = CircleShape,
            elevation = CardDefaults.cardElevation(4.dp),
            colors = CardDefaults.cardColors(colorResource(customization.colorIdWhite1))
        ) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(32.dp)
                    .padding(4.dp)
            )
        }
    }
}