
package ru.usedesk.knowledgebase_gui.compose

import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.animateDecay
import androidx.compose.animation.core.animateTo
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import ru.usedesk.knowledgebase_gui.screen.UsedeskKnowledgeBaseTheme
import kotlin.math.abs

internal class CustomToolbarScrollBehavior(
    val theme: UsedeskKnowledgeBaseTheme,
    val state: CustomToolbarScrollState,
    val flingAnimationSpec: DecayAnimationSpec<Float>?,
) {
    val nestedScrollConnection = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            // Don't intercept if scrolling down.
            if (available.y > 0f) return Offset.Zero

            val prevHeightOffset = state.heightOffset
            state.heightOffset = state.heightOffset + available.y
            return when (prevHeightOffset) {
                state.heightOffset -> Offset.Zero
                else -> available.copy(x = 0f)
                // We're in the middle of top app bar collapse or expand.
                // Consume only the scroll on the Y axis.
            }
        }

        override fun onPostScroll(
            consumed: Offset,
            available: Offset,
            source: NestedScrollSource,
        ): Offset {
            state.contentOffset += consumed.y

            if (available.y < 0f || consumed.y < 0f) {
                // When scrolling up, just update the state's height offset.
                val oldHeightOffset = state.heightOffset
                state.heightOffset = state.heightOffset + consumed.y
                return Offset(0f, state.heightOffset - oldHeightOffset)
            }

            if (consumed.y == 0f && available.y > 0) {
                // Reset the total content offset to zero when scrolling all the way down. This
                // will eliminate some float precision inaccuracies.
                state.contentOffset = 0f
            }

            if (available.y > 0f) {
                // Adjust the height offset in case the consumed delta Y is less than what was
                // recorded as available delta Y in the pre-scroll.
                val oldHeightOffset = state.heightOffset
                state.heightOffset = state.heightOffset + available.y
                return Offset(0f, state.heightOffset - oldHeightOffset)
            }
            return Offset.Zero
        }

        override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
            var result = super.onPostFling(consumed, available)
            // Check if the app bar is partially collapsed/expanded.
            // Note that we don't check for 0f due to float precision with the collapsedFraction
            // calculation.
            if (state.collapsedFraction > 0.01f && state.collapsedFraction < 1f) {
                result += state.flingToolbar(
                    initialVelocity = available.y,
                    flingAnimationSpec = flingAnimationSpec
                )
                state.snapToolbar(theme)
            }
            return result
        }
    }
}

private suspend fun CustomToolbarScrollState.flingToolbar(
    initialVelocity: Float,
    flingAnimationSpec: DecayAnimationSpec<Float>?,
): Velocity {
    var remainingVelocity = initialVelocity
    // In case there is an initial velocity that was left after a previous user fling, animate to
    // continue the motion to expand or collapse the app bar.
    if (flingAnimationSpec != null && abs(initialVelocity) > 1f) {
        var lastValue = 0f
        AnimationState(
            initialValue = 0f,
            initialVelocity = initialVelocity,
        ).animateDecay(flingAnimationSpec) {
            val delta = value - lastValue
            val initialHeightOffset = heightOffset
            heightOffset = initialHeightOffset + delta
            val consumed = abs(initialHeightOffset - heightOffset)
            lastValue = value
            remainingVelocity = this.velocity
            // avoid rounding errors and stop if anything is unconsumed
            if (abs(delta - consumed) > 0.5f) {
                cancelAnimation()
            }
        }
    }
    return Velocity(0f, remainingVelocity)
}

internal suspend fun CustomToolbarScrollState.snapToolbar(theme: UsedeskKnowledgeBaseTheme) {
    // In case the app bar motion was stopped in a state where it's partially visible, snap it to
    // the nearest state.
    if (heightOffset < 0 && heightOffset > heightOffsetLimit) {
        AnimationState(initialValue = heightOffset).animateTo(
            targetValue = if (collapsedFraction < 0.5f) 0f else heightOffsetLimit,
            animationSpec = theme.animationSpec()
        ) {
            heightOffset = value
        }
    }
}

@Composable
internal fun rememberToolbarScrollBehavior(theme: UsedeskKnowledgeBaseTheme) =
    CustomToolbarScrollBehavior(
        theme = theme,
        state = rememberToolbarScrollState(
            initialHeightOffsetLimit = -Float.MAX_VALUE
        ),
        flingAnimationSpec = rememberSplineBasedDecay()
    )