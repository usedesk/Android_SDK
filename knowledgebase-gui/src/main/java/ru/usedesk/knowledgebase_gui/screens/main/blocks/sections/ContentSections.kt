package ru.usedesk.knowledgebase_gui.screens.main.blocks.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.usedesk.knowledgebase_gui.R
import ru.usedesk.knowledgebase_gui.compose.LazyColumnCard
import ru.usedesk.knowledgebase_gui.compose.clickableItem
import ru.usedesk.knowledgebase_gui.compose.composeViewModel
import ru.usedesk.knowledgebase_sdk.entity.UsedeskSection

@Preview
@Composable
private fun Preview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.usedesk_white_2))
    ) {
        ContentSections(
            onSectionClicked = {}
        )
    }
}

@Composable
internal fun ContentSections(
    onSectionClicked: (UsedeskSection) -> Unit
) {
    val viewModel: SectionsViewModel = composeViewModel { SectionsViewModel() }
    val state by viewModel.modelFlow.collectAsState()
    LazyColumnCard {
        items(
            items = state.sections,
            key = UsedeskSection::id
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = colorResource(R.color.usedesk_white_1))
                    .clickableItem(
                        onClick = remember { { onSectionClicked(it) } }
                    )
                    .padding(
                        start = 10.dp,
                        end = 10.dp,
                        top = 8.dp,
                        bottom = 8.dp
                    )
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(color = colorResource(R.color.usedesk_gray_cold_1))
                ) {
                    BasicText(
                        modifier = Modifier
                            .align(Alignment.Center),
                        text = remember(it.title) {
                            it.title
                                .firstOrNull(Char::isLetterOrDigit)
                                ?.uppercase()
                                ?: ""
                        },
                        style = TextStyle(
                            fontSize = 17.sp,
                            color = colorResource(R.color.usedesk_black_2)
                        )
                    )
                }
                BasicText(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(
                            start = 10.dp,
                            end = 10.dp
                        )
                        .weight(weight = 1f, fill = true),
                    style = TextStyle(
                        fontSize = 17.sp,
                        textAlign = TextAlign.Start,
                        color = colorResource(R.color.usedesk_black_2)
                    ),
                    text = it.title
                )
                Icon(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .size(24.dp),
                    painter = painterResource(R.drawable.usedesk_ic_arrow_forward),
                    tint = Color.Unspecified,
                    contentDescription = null
                )
            }
        }
    }
}