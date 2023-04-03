package ru.usedesk.knowledgebase_gui.screen.blocks.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import ru.usedesk.common_sdk.UsedeskLog
import ru.usedesk.knowledgebase_gui.R
import ru.usedesk.knowledgebase_gui.compose.cardItem
import ru.usedesk.knowledgebase_gui.compose.clickableItem
import ru.usedesk.knowledgebase_gui.compose.kbUiViewModel
import ru.usedesk.knowledgebase_gui.screen.UsedeskKnowledgeBaseCustomization
import ru.usedesk.knowledgebase_gui.screen.attachToSupportButton
import ru.usedesk.knowledgebase_sdk.entity.UsedeskSection

@Preview
@Composable
private fun Preview() {
    val customization = UsedeskKnowledgeBaseCustomization()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(customization.colorIdWhite2))
    ) {
        ContentSections(
            customization = customization,
            viewModelStoreOwner = remember { { ViewModelStore() } },
            supportButtonVisible = remember { mutableStateOf(false) },
            onSectionClicked = {}
        )
    }
}

@Composable
internal fun ContentSections(
    customization: UsedeskKnowledgeBaseCustomization,
    viewModelStoreOwner: ViewModelStoreOwner,
    supportButtonVisible: MutableState<Boolean>,
    onSectionClicked: (UsedeskSection) -> Unit
) {
    val viewModel = kbUiViewModel(
        viewModelStoreOwner = viewModelStoreOwner
    ) { kbUiComponent -> SectionsViewModel(kbUiComponent.interactor) }
    UsedeskLog.onLog("ContentSections") { viewModel.toString() }
    val state by viewModel.modelFlow.collectAsState()
    state.lazyListState.attachToSupportButton(supportButtonVisible)
    LazyColumn(
        modifier = Modifier,
        state = state.lazyListState
    ) {
        items(
            items = state.sections,
            key = UsedeskSection::id
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 16.dp,
                        end = 16.dp
                    )
                    .cardItem(
                        customization = customization,
                        isTop = it == state.sections.firstOrNull(),
                        isBottom = it == state.sections.lastOrNull()
                    )
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
                        .background(color = colorResource(customization.colorIdGrayCold1))
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
                        style = customization.textStyleSectionTitleItem()
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
                    style = customization.textStyleSectionTextItem(),
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