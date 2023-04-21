
package ru.usedesk.knowledgebase_gui.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import ru.usedesk.knowledgebase_gui.screen.UsedeskKnowledgeBaseTheme

@Composable
internal fun ComposeTextField(
    theme: UsedeskKnowledgeBaseTheme,
    modifier: Modifier = Modifier,
    fieldModifier: Modifier = Modifier,
    enabled: Boolean = true,
    value: TextFieldValue,
    focusRequester: FocusRequester = remember { FocusRequester() },
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
            modifier = fieldModifier
                .focusRequester(focusRequester)
                .onFocusChanged(remember(onFocusChanged) { { onFocusChanged(it.isFocused) } }),
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
            enter = remember {
                fadeIn(theme.animationSpec()) +
                        slideInHorizontally(theme.animationSpec()) { it / 10 }
            },
            exit = remember {
                fadeOut(theme.animationSpec()) +
                        slideOutHorizontally(theme.animationSpec()) { it / 10 }
            }
        ) {
            BasicText(
                modifier = fieldModifier,
                text = placeholder,
                style = textStylePlaceholder
            )
        }
    }
}