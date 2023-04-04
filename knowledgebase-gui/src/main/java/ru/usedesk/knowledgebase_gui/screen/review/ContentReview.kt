package ru.usedesk.knowledgebase_gui.screen.review

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.usedesk.knowledgebase_gui.R
import ru.usedesk.knowledgebase_gui.compose.*
import ru.usedesk.knowledgebase_gui.screen.UsedeskKnowledgeBaseTheme

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
        modifier = Modifier.fillMaxSize()
    ) {
        val scrollState = rememberScrollState()
        LaunchedEffect(state.reviewFocused) {
            if (state.reviewFocused) {
                coroutineScope {
                    launch {
                        repeat(10) {
                            delay(100)
                            scrollState.scrollTo(Int.MAX_VALUE)
                        }
                    }
                }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState),
        ) {
            Replies(
                theme = theme,
                enabled = !state.buttonLoading,
                replies = stringArrayResource(theme.strings.arrayIdReviewTags),
                selectedReplies = state.selectedReplies,
                onReplySelected = viewModel::replySelected
            )
            ComposeTextField(
                modifier = Modifier
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = 16.dp,
                        bottom = 16.dp
                    )
                    .card(theme),
                fieldModifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 10.dp,
                        end = 10.dp,
                        top = 16.dp,
                        bottom = 16.dp,
                    ),
                value = state.reviewValue,
                enabled = !state.buttonLoading,
                placeholder = stringResource(theme.strings.textIdArticleReviewPlaceholder),
                textStyleText = theme.textStyles.articleReviewCommentText,
                textStylePlaceholder = theme.textStyles.articleReviewCommentPlaceholder,
                onValueChange = viewModel::reviewValueChanged,
                onFocusChanged = viewModel::reviewFocusChanged
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
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
            .padding(16.dp)
    ) {
        Layout(
            measurePolicy = flexMeasurePolicy(
                verticalInterval = 10.dp,
                horizontalInterval = 10.dp
            ),
            content = {
                replies.forEach { problem ->
                    Crossfade(
                        targetState = problem in selectedReplies
                    ) { active ->
                        BasicText(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    color = when {
                                        active -> theme.colors.black2
                                        else -> theme.colors.gray12
                                    }
                                )
                                .clickableItem(
                                    enabled = enabled,
                                    onClick = remember { { onReplySelected(problem) } }
                                )
                                .padding(
                                    start = 10.dp,
                                    end = 10.dp,
                                    top = 8.dp,
                                    bottom = 8.dp
                                ),
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
                .padding(16.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(color = theme.colors.black2)
                .clickableItem(
                    enabled = !loading,
                    onClick = onClick
                )
                .padding(12.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                when {
                    it.first -> Icon(
                        modifier = Modifier
                            .size(24.dp),
                        painter = painterResource(theme.drawables.iconIdReviewError),
                        contentDescription = null,
                        tint = Color.Unspecified
                    )
                    it.second -> CircularProgressIndicator(
                        modifier = Modifier
                            .size(24.dp),
                        strokeWidth = theme.dimensions.progressBarStrokeWidth,
                        color = theme.colors.red
                    )
                    else -> BasicText(
                        text = stringResource(theme.strings.textIdArticleReviewSend),
                        style = theme.textStyles.articleReviewSend
                    )
                }
            }
        }
    }
}