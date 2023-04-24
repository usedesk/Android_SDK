
package ru.usedesk.knowledgebase_gui.compose

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import ru.usedesk.knowledgebase_gui.screen.UsedeskKnowledgeBaseTheme
import java.lang.Integer.max
import java.lang.Integer.min

@Composable
internal fun ScreenNotLoaded(
    theme: UsedeskKnowledgeBaseTheme,
    tryAgain: () -> Unit,
    tryAgainVisible: Boolean?
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = theme.dimensions.rootPadding.start,
                end = theme.dimensions.rootPadding.end
            )
    ) {
        Layout(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            content = {
                Image(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(theme.dimensions.notLoadedImagePadding)
                        .align(Alignment.BottomCenter),
                    painter = painterResource(theme.drawables.imageCantLoad),
                    contentDescription = null,
                    contentScale = ContentScale.FillWidth
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    BasicText(
                        modifier = Modifier
                            .padding(theme.dimensions.notLoadedTitlePadding),
                        text = stringResource(theme.strings.loadError),
                        style = theme.textStyles.loadError
                    )
                    if (tryAgainVisible != null) {
                        Crossfade(
                            targetState = tryAgainVisible,
                            animationSpec = remember { theme.animationSpec() }
                        ) { visible ->
                            BasicText(
                                modifier = Modifier
                                    .clickableText(onClick = tryAgain),
                                text = when {
                                    visible -> stringResource(theme.strings.tryAgain)
                                    else -> " "
                                },
                                style = theme.textStyles.tryAgain
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(theme.dimensions.rootPadding.bottom)
                    )
                }
            },
            measurePolicy = { measurables, constraints ->
                val minSize = min(constraints.maxWidth, constraints.maxHeight)
                val topItemPlaceable = measurables[0].measure(
                    constraints.copy(
                        maxWidth = minSize,
                        maxHeight = minSize,
                        minWidth = 0,
                        minHeight = 0
                    )
                )
                val bottomItemPlaceable = measurables[1].measure(constraints)
                val sumHeight = topItemPlaceable.height + bottomItemPlaceable.height
                val maxItemHeight = max(topItemPlaceable.height, bottomItemPlaceable.height)
                val totalHeight: Int
                val bottomY: Int
                if (constraints.maxHeight >= maxItemHeight * 2) {
                    totalHeight = maxItemHeight * 2
                    bottomY = maxItemHeight
                } else if (constraints.maxHeight >= sumHeight) {
                    totalHeight = sumHeight
                    val bottomSpace = constraints.maxHeight - totalHeight
                    bottomY = constraints.maxHeight - bottomItemPlaceable.height - bottomSpace
                } else {
                    totalHeight = bottomItemPlaceable.height
                    bottomY = 0
                }
                val topY = bottomY - topItemPlaceable.height
                layout(
                    constraints.maxWidth,
                    totalHeight
                ) {
                    if (topY >= 0) {
                        topItemPlaceable.placeRelative(
                            (constraints.maxWidth - topItemPlaceable.width) / 2,
                            topY
                        )
                    }
                    bottomItemPlaceable.placeRelative(0, bottomY)
                }
            }
        )
    }
}