
package ru.usedesk.knowledgebase_gui.compose

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import ru.usedesk.knowledgebase_gui.screen.UsedeskKnowledgeBaseTheme
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
internal fun CustomToolbar(
    theme: UsedeskKnowledgeBaseTheme,
    title: String,
    scrollBehavior: CustomToolbarScrollBehavior,
    onBackPressed: () -> Unit
) {
    val collapsedFraction = scrollBehavior.state.collapsedFraction

    val fullyCollapsedTitleScale = theme.textStyles.toolbarCollapsedTitle.fontSize.value /
            theme.textStyles.toolbarExpandedTitle.fontSize.value

    val collapsingTitleScale = lerp(1f, fullyCollapsedTitleScale, collapsedFraction)

    val collapsedHeight = theme.dimensions.rootPadding.top +
            theme.dimensions.toolbarIconSize +
            theme.dimensions.toolbarBottomPadding

    Box(
        modifier = Modifier.padding(end = theme.dimensions.rootPadding.end)
    ) {
        Layout(
            modifier = Modifier
                .background(color = theme.colors.rootBackground)
                .heightIn(min = collapsedHeight),
            content = {
                Crossfade(
                    modifier = Modifier
                        .layoutId(ExpandedTitleId)
                        .fillMaxWidth()
                        .animateContentSize(animationSpec = remember { theme.animationSpec() }),
                    targetState = title,
                    animationSpec = remember { theme.animationSpec() }
                ) { title ->
                    Text(
                        modifier = Modifier
                            .wrapContentHeight(align = Alignment.Top)
                            .graphicsLayer(
                                scaleX = collapsingTitleScale,
                                scaleY = collapsingTitleScale,
                                transformOrigin = TransformOrigin(0f, 0f)
                            ),
                        text = title,
                        style = theme.textStyles.toolbarExpandedTitle
                    )
                }
                Crossfade(
                    modifier = Modifier
                        .layoutId(CollapsedTitleId)
                        .fillMaxWidth()
                        .animateContentSize(animationSpec = remember { theme.animationSpec() }),
                    targetState = title,
                    animationSpec = remember { theme.animationSpec() }
                ) { title ->
                    Text(
                        modifier = Modifier
                            .layoutId(CollapsedTitleId)
                            .wrapContentHeight(align = Alignment.Top)
                            .graphicsLayer(
                                scaleX = collapsingTitleScale,
                                scaleY = collapsingTitleScale,
                                transformOrigin = TransformOrigin(0f, 0f)
                            ),
                        text = title,
                        style = theme.textStyles.toolbarExpandedTitle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Box(
                    modifier = Modifier
                        .wrapContentSize()
                        .layoutId(NavigationIconId)
                        .clickableArea(
                            theme = theme,
                            onClick = onBackPressed
                        )
                ) {
                    Icon(
                        modifier = Modifier
                            .size(theme.dimensions.toolbarIconSize),
                        painter = painterResource(theme.drawables.iconBack),
                        tint = Color.Unspecified,
                        contentDescription = null
                    )
                }
            },
            measurePolicy = { measurables, constraints ->
                val intervalX = theme.dimensions.toolbarIntervalX.toPx()
                val intervalY = theme.dimensions.toolbarIntervalY.toPx()
                val startPadding = theme.dimensions.rootPadding.start.toPx()
                val endPadding = theme.dimensions.rootPadding.end.toPx()
                val topPadding = theme.dimensions.rootPadding.top.toPx()
                val bottomPadding = theme.dimensions.toolbarBottomPadding.toPx()

                // Measuring widgets inside toolbar:

                val navigationIconPlaceable = measurables.first { it.layoutId == NavigationIconId }
                    .measure(
                        constraints.copy(
                            maxWidth = (constraints.maxWidth - startPadding - endPadding).roundToInt(),
                            minWidth = 0,
                            minHeight = 0
                        )
                    )

                val expandedTitlePlaceable = measurables.first { it.layoutId == ExpandedTitleId }
                    .measure(
                        constraints.copy(
                            maxWidth = (constraints.maxWidth - startPadding - endPadding - intervalX).roundToInt(),
                            minWidth = 0,
                            minHeight = 0
                        )
                    )

                val navigationIconXOffset = navigationIconPlaceable.width + intervalX
                val navigationIconYOffset = navigationIconPlaceable.height + intervalY

                val collapsedTitleMaxWidthPx =
                    (constraints.maxWidth - navigationIconXOffset - startPadding - endPadding - intervalX) / fullyCollapsedTitleScale

                val collapsedTitlePlaceable = measurables.first { it.layoutId == CollapsedTitleId }
                    .measure(
                        constraints.copy(
                            maxWidth = collapsedTitleMaxWidthPx.roundToInt(),
                            minWidth = 0,
                            minHeight = 0
                        )
                    )

                // Calculating coordinates of widgets inside toolbar:

                // Measuring toolbar collapsing distance
                val heightOffsetLimitPx = expandedTitlePlaceable.height + intervalY
                if (scrollBehavior.state.heightOffsetLimit != -heightOffsetLimitPx) {
                    scrollBehavior.state.heightOffsetLimit = -heightOffsetLimitPx
                    scrollBehavior.state.heightOffset = collapsedFraction * (-heightOffsetLimitPx)
                }

                // Current coordinates of collapsing title
                val titleX = lerpsin(
                    0f,
                    navigationIconXOffset,
                    collapsedFraction
                ).roundToInt()

                val minTitleY =
                    (navigationIconPlaceable.height - collapsedTitlePlaceable.height * fullyCollapsedTitleScale) / 2f

                val titleY = lerp(
                    navigationIconYOffset,
                    minTitleY,
                    collapsedFraction
                ).roundToInt()

                val expandedToolbarHeightPx =
                    topPadding + navigationIconYOffset + expandedTitlePlaceable.height + bottomPadding

                val toolbarHeightPx = lerp(
                    expandedToolbarHeightPx,
                    collapsedHeight.toPx(),
                    collapsedFraction
                ).roundToInt()

                // Placing toolbar widgets:
                val totalTitleY = (topPadding + titleY).roundToInt()
                val totalTitleX = (startPadding + titleX).roundToInt()
                layout(constraints.maxWidth, toolbarHeightPx) {
                    navigationIconPlaceable.placeRelative(
                        x = startPadding.roundToInt(),
                        y = topPadding.roundToInt()
                    )
                    expandedTitlePlaceable.placeRelativeWithLayer(
                        x = totalTitleX,
                        y = totalTitleY,
                        layerBlock = {
                            alpha = sin((1f - collapsedFraction) * Math.PI / 2f).toFloat()
                        }
                    )
                    collapsedTitlePlaceable.placeRelativeWithLayer(
                        x = totalTitleX,
                        y = totalTitleY,
                        layerBlock = { alpha = sin(collapsedFraction * Math.PI / 2f).toFloat() }
                    )
                }
            }
        )
    }
}


private fun lerp(a: Float, b: Float, fraction: Float): Float = a + fraction * (b - a)

private fun lerpsin(a: Float, b: Float, fraction: Float): Float = lerp(
    a,
    b,
    sin(fraction * Math.PI / 2f).toFloat()
)

private fun lerpcos(a: Float, b: Float, fraction: Float): Float = lerp(
    a,
    b,
    cos(fraction * Math.PI / 2f).toFloat()
)

private const val ExpandedTitleId = "expandedTitle"
private const val CollapsedTitleId = "collapsedTitle"
private const val NavigationIconId = "navigationIcon"