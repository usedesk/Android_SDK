package ru.usedesk.knowledgebase_gui.screen.review

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.usedesk.knowledgebase_gui.R
import ru.usedesk.knowledgebase_gui.compose.*

private val replies = //TODO
    "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed venenatis sapien sed diam semper ullamcorper. Aenean aliquet posuere nibh sed laoreet. Sed quis elit quis ex tristique fermentum. Nam tristique urna ut sapien condimentum accumsan. Nulla egestas tincidunt massa vitae vulputate. Suspendisse vitae aliquam erat. Sed quis dui bibendum, tempus tellus vitae, varius ligula. Integer vitae ligula semper, aliquam neque vitae, hendrerit nisi. Sed ultrices elementum diam, porttitor gravida arcu viverra vitae. Ut ultricies, dolor et facilisis rhoncus, nisi diam pharetra dolor, mollis sodales diam massa non ex. Sed efficitur magna et efficitur viverra. Suspendisse porttitor ornare turpis vitae bibendum."
        .split(',', '.')
        .map(String::trim)
        .filter(String::isNotEmpty)
        .toSet()
        .toList()


internal const val REVIEW_KEY = "review"


@Composable
internal fun ContentReview(
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
                !state.buttonLoading,
                replies,
                state.selectedReplies,
                viewModel::replySelected
            )
            ReviewTextField(
                !state.buttonLoading,
                state.reviewValue,
                viewModel::reviewFocusChanged,
                viewModel::reviewValueChanged
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
            )
        }
        BottomButton(
            buttonShowed = state.buttonShowed,
            buttonLoading = state.buttonLoading,
            onClick = viewModel::sendClicked
        )
    }
}

@Composable
private fun Replies(
    enabled: Boolean,
    replies: List<String>,
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
                                    color = colorResource(
                                        when {
                                            active -> R.color.usedesk_black_2
                                            else -> R.color.usedesk_gray_12
                                        }
                                    )
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
                            style = TextStyle(
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Normal,
                                fontSize = 14.sp,
                                color = colorResource(
                                    when {
                                        active -> R.color.usedesk_white_2
                                        else -> R.color.usedesk_black_2
                                    }
                                )
                            )
                        )
                    }
                }
            }
        )
    }
}

@Composable
private fun ReviewTextField(
    enabled: Boolean,
    value: TextFieldValue,
    onFocusChanged: (Boolean) -> Unit,
    onValueChanged: (TextFieldValue) -> Unit
) {
    Box(
        modifier = Modifier
            .padding(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = 16.dp
            )
            .card()
    ) {
        val fieldModifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 10.dp,
                end = 10.dp,
                top = 16.dp,
                bottom = 16.dp,
            )
        BasicTextField(
            modifier = fieldModifier.onFocusChanged(remember { { onFocusChanged(it.isFocused) } }),
            value = value,
            onValueChange = onValueChanged,
            enabled = enabled,
            textStyle = TextStyle(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                color = colorResource(R.color.usedesk_black_2)
            ),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
        )

        AnimatedVisibility(
            modifier = Modifier,
            visible = value.text.isEmpty(),
            enter = fadeIn(spring(stiffness = Spring.StiffnessMedium)) + slideInHorizontally { it / 10 },
            exit = fadeOut(spring(stiffness = Spring.StiffnessMedium)) + slideOutHorizontally { it / 10 }
        ) {
            BasicText(
                modifier = fieldModifier,
                text = "Vash commentariy",
                style = TextStyle(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    color = colorResource(R.color.usedesk_gray_cold_2)
                )
            )
        }
    }
}

@Composable
private fun BoxScope.BottomButton(
    buttonShowed: Boolean,
    buttonLoading: Boolean,
    onClick: () -> Unit
) {
    AnimatedVisibility(
        modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter),
        visible = buttonShowed,
        enter = slideInVertically { it },
        exit = slideOutVertically { it }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(color = colorResource(R.color.usedesk_black_2))
                .clickableItem(
                    enabled = !buttonLoading,
                    onClick = onClick
                )
                .padding(12.dp)
        ) {
            when {
                buttonLoading -> CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(24.dp),
                )
                else -> BasicText(
                    modifier = Modifier.align(Alignment.Center),
                    text = stringResource(R.string.usedesk_send),
                    style = TextStyle(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Medium,
                        fontSize = 18.sp,
                        color = colorResource(R.color.usedesk_white_2)
                    )
                )
            }
        }
    }
}