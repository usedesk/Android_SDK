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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.usedesk.knowledgebase_gui.R
import ru.usedesk.knowledgebase_gui.screen.UsedeskKnowledgeBaseCustomization

@OptIn(ExperimentalAnimationApi::class)
@Composable
internal fun SearchBar(
    customization: UsedeskKnowledgeBaseCustomization,
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
                .background(colorResource(customization.colorIdGray1))
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
                painter = painterResource(R.drawable.usedesk_ic_search),
                tint = Color.Unspecified,
                contentDescription = null
            )
            ComposeTextField(
                modifier = Modifier.weight(weight = 1f, fill = true),
                fieldModifier = Modifier.fillMaxWidth(),
                value = value,
                placeholder = stringResource(customization.textIdSearchPlaceholder),
                textStyle = customization.textStyleSearch(),
                fieldTextColor = colorResource(customization.colorIdBlack2),
                placeholderTextColor = colorResource(customization.colorIdGray3),
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
                    painter = painterResource(R.drawable.usedesk_ic_cancel_round),
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
                        .clickableArea(onClick = it),
                    text = stringResource(customization.textIdSearchCancel),
                    style = TextStyle(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Normal,
                        fontSize = 17.sp,
                        color = colorResource(customization.colorIdBlue),
                        textAlign = TextAlign.Center
                    )
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
    textStyle: TextStyle,
    fieldTextColor: Color,
    placeholderTextColor: Color,
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
            textStyle = textStyle.copy(color = fieldTextColor),
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
                style = textStyle.copy(color = placeholderTextColor)
            )
        }
    }
}