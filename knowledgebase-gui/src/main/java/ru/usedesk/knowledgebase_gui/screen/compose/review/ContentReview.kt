package ru.usedesk.knowledgebase_gui.screen.compose.review

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import ru.usedesk.knowledgebase_gui.R
import ru.usedesk.knowledgebase_gui.compose.*
import ru.usedesk.knowledgebase_gui.screen.UsedeskKnowledgeBaseTheme
import ru.usedesk.knowledgebase_gui.screen.compose.ComposeTextField

internal const val REVIEW_KEY = "review"


@Composable
internal fun ContentReview(
    theme: UsedeskKnowledgeBaseTheme,
    viewModelStoreFactory: ViewModelStoreFactory,
    articleId: Long,
    goBack: () -> Unit
) {
    val viewModel = kbUiViewModel(
        key = remember(articleId) { articleId.toString() },
        viewModelStoreOwner = remember { { viewModelStoreFactory.get(REVIEW_KEY) } }
    ) { kbUiComponent -> ReviewViewModel(kbUiComponent.interactor, articleId) }
    val state by viewModel.modelFlow.collectAsState()

    state.goBack?.use { goBack() }

    val focusManager = LocalFocusManager.current
    state.clearFocus?.use { focusManager.clearFocus() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
    ) {
        val scrollState = rememberScrollState()
        val coroutineScope = rememberCoroutineScope()
        KeyboardListener { visible ->
            if (visible) {
                coroutineScope.launch {
                    scrollState.animateScrollTo(Int.MAX_VALUE)
                }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(
                    start = theme.dimensions.rootPadding.start,
                    end = theme.dimensions.rootPadding.end
                ),
        ) {
            Replies(
                theme = theme,
                enabled = !state.buttonLoading,
                replies = stringArrayResource(theme.strings.reviewTags),
                selectedReplies = state.selectedReplies,
                onReplySelected = viewModel::replySelected
            )
            ComposeTextField(
                modifier = Modifier
                    .padding(
                        bottom = theme.dimensions.articleReviewSendHeight +
                                theme.dimensions.rootPadding.top +
                                theme.dimensions.rootPadding.bottom
                    )
                    .card(theme),
                fieldModifier = Modifier
                    .fillMaxWidth()
                    .padding(theme.dimensions.articleReviewCommentInnerPadding),
                value = state.reviewValue,
                enabled = !state.buttonLoading,
                placeholder = stringResource(theme.strings.articleReviewPlaceholder),
                textStyleText = theme.textStyles.articleReviewCommentText,
                textStylePlaceholder = theme.textStyles.articleReviewCommentPlaceholder,
                singleLine = false,
                onValueChange = viewModel::reviewValueChanged,
                onFocusChanged = viewModel::reviewFocusChanged
            )
        }
        val tagsPrefix = stringResource(R.string.usedesk_review_tags_prefix)
        val commentPrefix = stringResource(R.string.usedesk_review_comment_prefix)
        BottomButton(
            theme = theme,
            showed = state.buttonShowed,
            error = state.buttonError,
            loading = state.buttonLoading,
            onClick = remember { { viewModel.sendClicked(tagsPrefix, commentPrefix) } }
        )
    }
}

@Composable
private fun Replies(
    theme: UsedeskKnowledgeBaseTheme,
    enabled: Boolean,
    replies: Array<String>,
    selectedReplies: List<String>,
    onReplySelected: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = theme.dimensions.articleReviewTagsBottomPadding)
    ) {
        Layout(
            measurePolicy = flexMeasurePolicy(
                verticalInterval = theme.dimensions.articleReviewTagsVerticalInterval,
                horizontalInterval = theme.dimensions.articleReviewTagsHorizontalInterval
            ),
            content = {
                replies.forEach { problem ->
                    Crossfade(
                        targetState = problem in selectedReplies
                    ) { active ->
                        BasicText(
                            modifier = Modifier
                                .clip(RoundedCornerShape(theme.dimensions.articleReviewTagCornerRadius))
                                .background(
                                    color = when {
                                        active -> theme.colors.articleReviewTagSelectedBackground
                                        else -> theme.colors.articleReviewTagUnselectedBackground
                                    }
                                )
                                .clickableItem(
                                    enabled = enabled,
                                    onClick = remember { { onReplySelected(problem) } }
                                )
                                .padding(theme.dimensions.articleReviewTagInnerPadding),
                            text = problem,
                            style = when {
                                active -> theme.textStyles.articleReviewTag
                                else -> theme.textStyles.articleReviewTagSelected
                            }
                        )
                    }
                }
            }
        )
    }
}

@Composable
private fun BoxScope.BottomButton(
    theme: UsedeskKnowledgeBaseTheme,
    showed: Boolean,
    error: Boolean,
    loading: Boolean,
    onClick: () -> Unit
) {
    AnimatedVisibility(
        modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter),
        visible = showed,
        enter = slideInVertically { it },
        exit = slideOutVertically { it }
    ) {
        Crossfade(
            targetState = remember(error, loading) { Pair(error, loading) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(theme.dimensions.rootPadding)
                .clip(RoundedCornerShape(theme.dimensions.articleReviewSendCornerRadius))
                .background(color = theme.colors.articleReviewSendBackground)
                .clickableItem(
                    enabled = !loading,
                    onClick = onClick
                )
                .height(theme.dimensions.articleReviewSendHeight)
                .padding(theme.dimensions.articleReviewSendPadding)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                when {
                    it.first -> Icon(
                        modifier = Modifier
                            .size(theme.dimensions.articleReviewSendIconSize),
                        painter = painterResource(theme.drawables.iconReviewError),
                        contentDescription = null,
                        tint = Color.Unspecified
                    )
                    it.second -> CircularProgressIndicator(
                        modifier = Modifier
                            .size(theme.dimensions.articleReviewSendIconSize),
                        strokeWidth = theme.dimensions.progressBarStrokeWidth,
                        color = theme.colors.progressBarIndicator
                    )
                    else -> BasicText(
                        text = stringResource(theme.strings.articleReviewSend),
                        style = theme.textStyles.articleReviewSend
                    )
                }
            }
        }
    }
}