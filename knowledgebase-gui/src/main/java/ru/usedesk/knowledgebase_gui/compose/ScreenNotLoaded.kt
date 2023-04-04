package ru.usedesk.knowledgebase_gui.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.usedesk.knowledgebase_gui.screen.UsedeskKnowledgeBaseTheme

@Composable
internal fun ScreenNotLoaded(
    theme: UsedeskKnowledgeBaseTheme,
    tryAgain: (() -> Unit)?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Image(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                painter = painterResource(theme.drawables.imageIdCantLoad),
                contentDescription = null,
                contentScale = ContentScale.FillWidth
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(
                    start = 24.dp,
                    end = 24.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BasicText(
                modifier = Modifier
                    .padding(top = 12.dp),
                text = stringResource(theme.strings.textIdLoadError),
                style = theme.textStyles.loadError
            )
            AnimatedVisibility(
                modifier = Modifier
                    .padding(top = 8.dp),
                visible = tryAgain != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                BasicText(
                    modifier = Modifier
                        .clickableText(onClick = tryAgain ?: remember { {} }),
                    text = stringResource(theme.strings.textIdTryAgain),
                    style = theme.textStyles.tryAgain
                )
            }
        }
    }
}