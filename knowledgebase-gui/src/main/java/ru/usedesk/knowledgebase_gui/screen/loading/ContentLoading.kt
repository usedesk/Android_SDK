package ru.usedesk.knowledgebase_gui.screen.loading

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ru.usedesk.knowledgebase_gui._entity.ContentState
import ru.usedesk.knowledgebase_gui.compose.CardCircleProgress
import ru.usedesk.knowledgebase_gui.compose.ScreenNotLoaded
import ru.usedesk.knowledgebase_gui.compose.ViewModelStoreFactory
import ru.usedesk.knowledgebase_gui.compose.kbUiViewModel
import ru.usedesk.knowledgebase_gui.screen.UsedeskKnowledgeBaseCustomization

internal const val LOADING_KEY = "article"

@Composable
internal fun ContentLoading(
    customization: UsedeskKnowledgeBaseCustomization,
    viewModelStoreFactory: ViewModelStoreFactory,
    tryAgain: () -> Unit
) {
    val viewModel = kbUiViewModel(
        viewModelStoreOwner = remember { { viewModelStoreFactory.get(LOADING_KEY) } }
    ) { kbUiComponent -> LoadingViewModel(kbUiComponent.interactor) }
    val state by viewModel.modelFlow.collectAsState()
    Box(modifier = Modifier.fillMaxSize()) {
        Crossfade(targetState = state.contentState) { contentState ->
            when (contentState) {
                is ContentState.Empty,
                is ContentState.Loaded -> Box(modifier = Modifier.fillMaxSize())
                is ContentState.Error -> ScreenNotLoaded(
                    customization = customization,
                    tryAgain = if (!state.loading) tryAgain else null
                )
            }
        }
        CardCircleProgress(
            customization = customization,
            modifier = Modifier.align(Alignment.TopCenter),
            visible = state.loading
        )
    }
}