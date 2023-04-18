package ru.usedesk.knowledgebase_gui.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import ru.usedesk.knowledgebase_gui.screen.UsedeskKnowledgeBaseTheme

@Composable
internal fun ScreenNotLoaded(
    theme: UsedeskKnowledgeBaseTheme,
    tryAgain: (() -> Unit)?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(theme.dimensions.rootPadding)
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
                painter = painterResource(theme.drawables.imageCantLoad),
                contentDescription = null,
                contentScale = ContentScale.FillWidth
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BasicText(
                modifier = Modifier
                    .padding(theme.dimensions.notLoadedErrorPadding),
                text = stringResource(theme.strings.loadError),
                style = theme.textStyles.loadError
            )
            AnimatedVisibility(
                visible = tryAgain != null,
                enter = remember { fadeIn(theme.animationSpec()) },
                exit = remember { fadeOut(theme.animationSpec()) }
            ) {
                BasicText(
                    modifier = Modifier
                        .clickableText(onClick = tryAgain ?: remember { {} }),
                    text = stringResource(theme.strings.tryAgain),
                    style = theme.textStyles.tryAgain
                )
            }
        }
    }
}