
package ru.usedesk.knowledgebase_gui.screen.compose

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import ru.usedesk.knowledgebase_gui.compose.*
import ru.usedesk.knowledgebase_gui.screen.UsedeskKnowledgeBaseTheme

@Preview
@Composable
private fun Preview() {
    val theme = UsedeskKnowledgeBaseTheme.provider()
    SearchBar(
        theme = theme,
        value = TextFieldValue(),
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
    value: TextFieldValue,
    focused: Boolean,
    onClearClick: () -> Unit,
    onCancelClick: (() -> Unit)?,
    onValueChange: (TextFieldValue) -> Unit,
    onSearchBarClicked: () -> Unit,
    onSearch: () -> Unit
) {
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
                value = value,
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
                value.text.isNotEmpty(),
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
