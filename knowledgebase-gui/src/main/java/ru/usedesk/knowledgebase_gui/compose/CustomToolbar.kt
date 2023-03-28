package ru.usedesk.knowledgebase_gui.compose

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.usedesk.knowledgebase_gui.R
import kotlin.math.roundToInt

@Composable
internal fun CustomToolbar(
    modifier: Modifier = Modifier,
    title: String,
    scrollBehavior: CustomToolbarScrollBehavior,
    onBackPressed: () -> Unit
) {
    val collapsedFraction = scrollBehavior.state.collapsedFraction

    val textStyle = MaterialTheme.typography.headlineLarge

    val fullyCollapsedTitleScale =
        CollapsedTitleLineHeight.value / textStyle.lineHeight.value

    val collapsingTitleScale = lerp(1f, fullyCollapsedTitleScale, collapsedFraction)

    Surface(modifier = modifier.animateContentSize()) {
        Layout(
            content = {
                Crossfade(
                    modifier = Modifier.layoutId(ExpandedTitleId),
                    targetState = title
                ) { title ->
                    Text(
                        modifier = Modifier
                            .layoutId(ExpandedTitleId)
                            .wrapContentHeight(align = Alignment.Top)
                            .graphicsLayer(
                                scaleX = collapsingTitleScale,
                                scaleY = collapsingTitleScale,
                                transformOrigin = TransformOrigin(0f, 0f)
                            ),
                        text = title,
                        style = textStyle
                    )
                }
                Crossfade(
                    modifier = Modifier.layoutId(CollapsedTitleId),
                    targetState = title
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
                        style = textStyle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Box(
                    modifier = Modifier
                        .wrapContentSize()
                        .layoutId(NavigationIconId)
                        .clickableArea(
                            radius = 30.dp,
                            onClick = onBackPressed
                        )
                ) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(R.drawable.usedesk_ic_back),
                        tint = Color.Unspecified,
                        contentDescription = null
                    )
                }
            },
            modifier = modifier.then(Modifier.heightIn(min = MinCollapsedHeight)),
            measurePolicy = { measurables, constraints ->
                val horizontalPaddingPx = HorizontalPadding.toPx()
                val expandedTitleBottomPaddingPx = ExpandedTitleBottomPadding.toPx()

                // Measuring widgets inside toolbar:

                val navigationIconPlaceable = measurables.first { it.layoutId == NavigationIconId }
                    .measure(constraints.copy(minWidth = 0))

                val expandedTitlePlaceable = measurables.first { it.layoutId == ExpandedTitleId }
                    .measure(
                        constraints.copy(
                            maxWidth = (constraints.maxWidth - 2 * horizontalPaddingPx).roundToInt(),
                            minWidth = 0,
                            minHeight = 0
                        )
                    )

                val navigationIconOffset = navigationIconPlaceable.width + horizontalPaddingPx * 2

                val collapsedTitleMaxWidthPx =
                    (constraints.maxWidth - navigationIconOffset - horizontalPaddingPx) / fullyCollapsedTitleScale

                val collapsedTitlePlaceable = measurables.first { it.layoutId == CollapsedTitleId }
                    .measure(
                        constraints.copy(
                            maxWidth = collapsedTitleMaxWidthPx.roundToInt(),
                            minWidth = 0,
                            minHeight = 0
                        )
                    )

                val collapsedHeightPx = MinCollapsedHeight.toPx()

                var layoutHeightPx = collapsedHeightPx

                // Calculating coordinates of widgets inside toolbar:

                // Current coordinates of navigation icon
                val navigationIconX = horizontalPaddingPx.roundToInt()
                val navigationIconY =
                    ((collapsedHeightPx - navigationIconPlaceable.height) / 2).roundToInt()

                // Current coordinates of title
                var collapsingTitleY = 0
                var collapsingTitleX = 0

                // Measuring toolbar collapsing distance
                val heightOffsetLimitPx =
                    expandedTitlePlaceable.height + expandedTitleBottomPaddingPx
                if (scrollBehavior.state.heightOffsetLimit != -heightOffsetLimitPx) {
                    scrollBehavior.state.heightOffsetLimit = -heightOffsetLimitPx
                    scrollBehavior.state.heightOffset = collapsedFraction * (-heightOffsetLimitPx)
                }

                // Toolbar height at fully expanded state
                val fullyExpandedHeightPx = MinCollapsedHeight.toPx() + heightOffsetLimitPx

                // Coordinates of fully expanded title
                val fullyExpandedTitleX = horizontalPaddingPx
                val fullyExpandedTitleY =
                    fullyExpandedHeightPx - expandedTitlePlaceable.height - expandedTitleBottomPaddingPx

                // Coordinates of fully collapsed title
                val fullyCollapsedTitleX = navigationIconOffset
                val fullyCollapsedTitleY =
                    collapsedHeightPx / 2 - CollapsedTitleLineHeight.toPx().roundToInt() / 2

                // Current height of toolbar
                layoutHeightPx = lerp(fullyExpandedHeightPx, collapsedHeightPx, collapsedFraction)

                // Current coordinates of collapsing title
                collapsingTitleX = lerp(
                    fullyExpandedTitleX,
                    fullyCollapsedTitleX,
                    collapsedFraction
                ).roundToInt()
                collapsingTitleY = lerp(
                    fullyExpandedTitleY,
                    fullyCollapsedTitleY,
                    collapsedFraction
                ).roundToInt()

                val toolbarHeightPx = layoutHeightPx.roundToInt()


                // Placing toolbar widgets:

                layout(constraints.maxWidth, toolbarHeightPx) {
                    navigationIconPlaceable.placeRelative(
                        x = navigationIconX,
                        y = navigationIconY
                    )
                    expandedTitlePlaceable.placeRelativeWithLayer(
                        x = collapsingTitleX,
                        y = collapsingTitleY,
                        layerBlock = { alpha = 1f - collapsedFraction }
                    )
                    collapsedTitlePlaceable.placeRelativeWithLayer(
                        x = collapsingTitleX,
                        y = collapsingTitleY,
                        layerBlock = { alpha = collapsedFraction }
                    )
                }
            }
        )
    }
}


private fun lerp(a: Float, b: Float, fraction: Float): Float = a + fraction * (b - a)

private val MinCollapsedHeight = 56.dp
private val HorizontalPadding = 16.dp
private val ExpandedTitleBottomPadding = 8.dp
private val CollapsedTitleLineHeight = 28.sp

private const val ExpandedTitleId = "expandedTitle"
private const val CollapsedTitleId = "collapsedTitle"
private const val NavigationIconId = "navigationIcon"