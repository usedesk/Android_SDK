package ru.usedesk.knowledgebase_gui.screen.compose.blocks.sections

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import ru.usedesk.knowledgebase_gui.compose.*
import ru.usedesk.knowledgebase_gui.screen.UsedeskKnowledgeBaseTheme
import ru.usedesk.knowledgebase_sdk.entity.UsedeskSection

@Preview
@Composable
private fun Preview() {
    val theme = UsedeskKnowledgeBaseTheme()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = theme.colors.rootBackground)
    ) {
        ContentSections(
            theme = theme,
            viewModelStoreOwner = remember { { ViewModelStore() } },
            supportButtonVisible = remember { mutableStateOf(false) },
            onSectionClicked = {}
        )
    }
}

@Composable
internal fun ContentSections(
    theme: UsedeskKnowledgeBaseTheme,
    viewModelStoreOwner: ViewModelStoreOwner,
    supportButtonVisible: MutableState<Boolean>,
    onSectionClicked: (UsedeskSection) -> Unit
) {
    val viewModel = kbUiViewModel(
        viewModelStoreOwner = viewModelStoreOwner
    ) { kbUiComponent -> SectionsViewModel(kbUiComponent.interactor) }
    val state by viewModel.modelFlow.collectAsState()
    supportButtonVisible.value = state.lazyListState.isSupportButtonVisible()
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
                    .cardItem(
                        theme = theme,
                        isTop = it == state.sections.firstOrNull(),
                        isBottom = it == state.sections.lastOrNull()
                    )
                    .clickableItem(
                        onClick = remember { { onSectionClicked(it) } }
                    )
                    .padding(theme.dimensions.sectionsItemInnerPadding)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .size(theme.dimensions.sectionsItemIconSize)
                        .clip(CircleShape)
                        .background(color = theme.colors.sectionsIconBackground)
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
                        style = theme.textStyles.sectionTitleItem
                    )
                }
                BasicText(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(theme.dimensions.sectionsItemTitlePadding)
                        .weight(weight = 1f, fill = true),
                    style = theme.textStyles.sectionTextItem,
                    text = it.title
                )
                Icon(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .size(theme.dimensions.sectionsItemArrowSize),
                    painter = painterResource(theme.drawables.iconListItemArrowForward),
                    tint = Color.Unspecified,
                    contentDescription = null
                )
            }
        }
    }
}