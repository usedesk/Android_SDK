package ru.usedesk.knowledgebase_gui.screen.compose

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import ru.usedesk.knowledgebase_gui.compose.clickableItem
import ru.usedesk.knowledgebase_gui.compose.clickableText
import ru.usedesk.knowledgebase_gui.compose.padding
import ru.usedesk.knowledgebase_gui.screen.UsedeskKnowledgeBaseTheme

@OptIn(ExperimentalAnimationApi::class)
@Composable
internal fun SearchBar(
    theme: UsedeskKnowledgeBaseTheme,
    value: TextFieldValue,
    onClearClick: () -> Unit,
    onCancelClick: (() -> Unit)?,
    onValueChange: (TextFieldValue) -> Unit,
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
            ComposeTextField(
                modifier = Modifier
                    .weight(weight = 1f, fill = true)
                    .padding(theme.dimensions.searchBarQueryPadding),
                fieldModifier = Modifier.fillMaxWidth(),
                value = value,
                placeholder = stringResource(theme.strings.searchPlaceholder),
                textStyleText = theme.textStyles.searchText,
                textStylePlaceholder = theme.textStyles.searchPlaceholder,
                imeAction = ImeAction.Search,
                keyboardActions = KeyboardActions(onSearch = remember { { onSearch() } }),
                singleLine = true,
                onValueChange = onValueChange
            )
            AnimatedVisibility(
                value.text.isNotEmpty(),
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
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
                .animateContentSize()
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

@Composable
internal fun ComposeTextField(
    modifier: Modifier = Modifier,
    fieldModifier: Modifier = Modifier,
    enabled: Boolean = true,
    value: TextFieldValue,
    placeholder: String,
    textStyleText: TextStyle,
    textStylePlaceholder: TextStyle,
    imeAction: ImeAction = ImeAction.Done,
    keyboardActions: KeyboardActions = KeyboardActions(),
    singleLine: Boolean,
    onValueChange: (TextFieldValue) -> Unit,
    onFocusChanged: (Boolean) -> Unit = remember { {} }
) {
    Box(modifier = modifier) {
        BasicTextField(
            modifier = fieldModifier.onFocusChanged(remember { { onFocusChanged(it.isFocused) } }),
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            textStyle = textStyleText,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = imeAction
            ),
            keyboardActions = keyboardActions,
            singleLine = singleLine
        )

        AnimatedVisibility(
            modifier = Modifier,
            visible = value.text.isEmpty(),
            enter = fadeIn(spring(stiffness = Spring.StiffnessMedium)) + slideInHorizontally { it / 10 },
            exit = fadeOut(spring(stiffness = Spring.StiffnessMedium)) + slideOutHorizontally { it / 10 }
        ) {
            BasicText(
                modifier = fieldModifier,
                text = placeholder,
                style = textStylePlaceholder
            )
        }
    }
}