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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.usedesk.knowledgebase_gui.R
import ru.usedesk.knowledgebase_gui.compose.clickableItem
import ru.usedesk.knowledgebase_gui.compose.composeViewModel
import ru.usedesk.knowledgebase_gui.compose.flexMeasurePolicy

val problems = //TODO
    listOf(
        "123123123123123",
        "456456",
        "789789789789789789789789789",
        "111111111",
        "222",
        "333",
        "333"
    )

@Composable
internal fun ContentReview(
    articleId: Long,
    onReviewSent: () -> Unit
) {
    val viewModel = composeViewModel(articleId.toString()) { ReviewViewModel(articleId) }
    val state by viewModel.modelFlow.collectAsState()
    state.done?.use { onReviewSent() }
    Box(modifier = Modifier.clipToBounds()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
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
                        problems.forEach { problem ->
                            Crossfade(
                                targetState = problem in state.selectedProblems
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
                                        .clickableItem(onClick = remember {
                                            {
                                                viewModel.problemSelected(problem)
                                            }
                                        })
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
            BasicTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.reviewValue,
                onValueChange = viewModel::reviewValueChanged,
                textStyle = TextStyle()
            )
        }
        AnimatedVisibility(
            modifier = Modifier.align(Alignment.BottomCenter),
            visible = state.buttonShowed,
            enter = slideInVertically { it },
            exit = slideOutVertically { it }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color = colorResource(R.color.usedesk_black_2))
                    .clickableItem(onClick = viewModel::sendClicked)
                    .padding(12.dp)
            ) {
                when {
                    state.buttonLoading -> CircularProgressIndicator(
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
}