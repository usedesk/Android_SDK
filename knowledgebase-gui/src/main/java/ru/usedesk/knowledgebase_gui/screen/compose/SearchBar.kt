
package ru.usedesk.knowledgebase_gui.screen.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.delay
import ru.usedesk.knowledgebase_gui.compose.ComposeTextField
import ru.usedesk.knowledgebase_gui.compose.clickableItem
import ru.usedesk.knowledgebase_gui.compose.clickableText
import ru.usedesk.knowledgebase_gui.compose.padding
import ru.usedesk.knowledgebase_gui.compose.update
import ru.usedesk.knowledgebase_gui.screen.UsedeskKnowledgeBaseTheme

@Preview
@Composable
private fun Preview() {
    val theme = UsedeskKnowledgeBaseTheme.provider()
    SearchBar(
        theme = theme,
        value = { TextFieldValue() },
        focused = false,
        onClearClick = {},
        onCancelClick = {},
        onValueChange = {},
        onSearchBarClicked = {},
        onSearch = {}
    )
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
internal fun SearchBar(
    theme: UsedeskKnowledgeBaseTheme,
    value: () -> TextFieldValue,
    focused: Boolean,
    onClearClick: () -> Unit,
    onCancelClick: (() -> Unit)?,
    onValueChange: (TextFieldValue) -> Unit,
    onSearchBarClicked: () -> Unit,
    onSearch: () -> Unit
) {
    // Deferred read: invoking value() here (not in the parent) keeps text-change snapshot
    // subscriptions scoped to SearchBar — ContentBlocks/ComposeRoot don't see them.
    val currentValue = value()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = theme.dimensions.rootPadding.start,
                end = theme.dimensions.rootPadding.end,
                bottom = theme.dimensions.searchBarBottomPadding
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(weight = 1f, fill = true)
                .clip(RoundedCornerShape(theme.dimensions.searchBarCornerRadius))
                .background(color = theme.colors.searchBarBackground)
                .padding(theme.dimensions.searchBarInnerPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier
                    .size(theme.dimensions.searchBarIconSize),
                painter = painterResource(theme.drawables.iconSearch),
                tint = Color.Unspecified,
                contentDescription = null
            )
            val focusRequester = remember { FocusRequester() }
            ComposeTextField(
                theme = theme,
                modifier = Modifier
                    .weight(weight = 1f, fill = true)
                    .update {
                        when {
                            focused -> this
                            else -> clickableText(onClick = onSearchBarClicked)
                        }
                    }
                    .padding(theme.dimensions.searchBarQueryPadding),
                enabled = focused,
                fieldModifier = Modifier.fillMaxWidth(),
                value = currentValue,
                focusRequester = focusRequester,
                placeholder = stringResource(theme.strings.searchPlaceholder),
                textStyleText = theme.textStyles.searchText,
                textStylePlaceholder = theme.textStyles.searchPlaceholder,
                imeAction = ImeAction.Search,
                keyboardActions = KeyboardActions(onSearch = remember(onSearch) { { onSearch() } }),
                singleLine = true,
                onValueChange = onValueChange
            )
            LaunchedEffect(focused) {
                if (focused) {
                    delay(100)
                    focusRequester.requestFocus()
                }
            }
            AnimatedVisibility(
                currentValue.text.isNotEmpty(),
                enter = remember { fadeIn(theme.animationSpec()) + scaleIn(theme.animationSpec()) },
                exit = remember { fadeOut(theme.animationSpec()) + scaleOut(theme.animationSpec()) }
            ) {
                Icon(
                    modifier = Modifier
                        .size(theme.dimensions.searchBarIconSize)
                        .clip(CircleShape)
                        .clickableItem(onClick = onClearClick),
                    painter = painterResource(theme.drawables.iconSearchCancel),
                    tint = Color.Unspecified,
                    contentDescription = null
                )
            }
        }
        Crossfade(
            targetState = onCancelClick,
            modifier = Modifier
                .animateContentSize(animationSpec = remember { theme.animationSpec() }),
            animationSpec = remember { theme.animationSpec() }
        ) {
            when (it) {
                null -> Box(modifier = Modifier)
                else -> BasicText(
                    modifier = Modifier
                        .padding(start = theme.dimensions.searchBarCancelInterval)
                        .clickableText(onClick = it),
                    text = stringResource(theme.strings.searchCancel),
                    style = theme.textStyles.searchCancel
                )
            }
        }
    }
}
