package ru.usedesk.knowledgebase_gui.compose

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
import androidx.compose.ui.unit.dp
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
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(weight = 1f, fill = true)
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 16.dp
                )
                .clip(RoundedCornerShape(10.dp))
                .background(theme.colors.gray1)
                .padding(
                    start = 6.dp,
                    end = 8.dp,
                    top = 6.dp,
                    bottom = 6.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier
                    .padding(end = 4.dp)
                    .size(20.dp),
                painter = painterResource(theme.drawables.iconIdSearch),
                tint = Color.Unspecified,
                contentDescription = null
            )
            ComposeTextField(
                modifier = Modifier.weight(weight = 1f, fill = true),
                fieldModifier = Modifier.fillMaxWidth(),
                value = value,
                placeholder = stringResource(theme.strings.textIdSearchPlaceholder),
                textStyleText = theme.textStyles.searchText,
                textStylePlaceholder = theme.textStyles.searchPlaceholder,
                imeAction = ImeAction.Search,
                keyboardActions = KeyboardActions(onSearch = remember { { onSearch() } }),
                onValueChange = onValueChange
            )
            AnimatedVisibility(
                value.text.isNotEmpty(),
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                Icon(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .clickableItem(onClick = onClearClick),
                    painter = painterResource(theme.drawables.iconIdSearchCancel),
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
                        .padding(
                            top = 6.dp,
                            bottom = 22.dp,
                            end = 16.dp
                        )
                        .clickableText(onClick = it),
                    text = stringResource(theme.strings.textIdSearchCancel),
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
            keyboardActions = keyboardActions
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