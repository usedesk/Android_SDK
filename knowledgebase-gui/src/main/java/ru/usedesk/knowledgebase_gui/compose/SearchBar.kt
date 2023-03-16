package ru.usedesk.knowledgebase_gui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import ru.usedesk.knowledgebase_gui.R

@Composable
internal fun SearchBar(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
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
            )
    ) {
        Icon(
            modifier = Modifier
                .padding(end = 2.dp)
                .align(Alignment.CenterVertically),
            painter = painterResource(R.drawable.usedesk_ic_search),
            tint = Color.Unspecified,
            contentDescription = null
        )
        BasicTextField(
            modifier = Modifier
                .align(Alignment.CenterVertically),
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle()
        )
    }
}