package ru.usedesk.knowledgebase_gui.compose

import androidx.compose.animation.*
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

@OptIn(ExperimentalAnimationApi::class)
@Composable
internal fun SearchBar(
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
                .background(colorResource(R.color.usedesk_gray_12))
                .padding(
                    start = 8.dp,
                    end = 8.dp,
                    top = 6.dp,
                    bottom = 6.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier
                    .padding(end = 2.dp)
                    .size(20.dp),
                painter = painterResource(R.drawable.usedesk_ic_search),
                tint = Color.Unspecified,
                contentDescription = null
            )
            BasicTextField(
                modifier = Modifier
                    .weight(weight = 1f, fill = true),
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Normal,
                    fontSize = 17.sp,
                    color = colorResource(R.color.usedesk_black_3)
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(onSearch = remember { { onSearch() } })
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
                    text = stringResource(R.string.usedesk_cancel),
                    style = TextStyle(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Normal,
                        fontSize = 17.sp,
                        color = colorResource(R.color.usedesk_blue),
                        textAlign = TextAlign.Center
                    )
                )
            }
        }
    }
}