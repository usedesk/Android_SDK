package ru.usedesk.knowledgebase_gui.screen.compose.loading

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ru.usedesk.knowledgebase_gui._entity.ContentState
import ru.usedesk.knowledgebase_gui.compose.*
import ru.usedesk.knowledgebase_gui.screen.UsedeskKnowledgeBaseTheme

@Composable
internal fun ContentLoading(
    theme: UsedeskKnowledgeBaseTheme,
    viewModelStoreFactory: ViewModelStoreFactory,
    tryAgain: () -> Unit
) {
    val viewModel = kbUiViewModel(
        viewModelStoreOwner = remember { { viewModelStoreFactory.get(StoreKeys.LOADING.name) } }
    ) { kbUiComponent -> LoadingViewModel(kbUiComponent.interactor) }
    DisposableEffect(Unit) {
        onDispose {
            viewModelStoreFactory.clear(StoreKeys.LOADING.name)
        }
    }
    val state by viewModel.modelFlow.collectAsState()
    Box(modifier = Modifier.fillMaxSize()) {
        Crossfade(targetState = state.contentState,
            animationSpec = remember { theme.animationSpec() }) { contentState ->
            when (contentState) {
                is ContentState.Empty,
                is ContentState.Loaded -> Box(modifier = Modifier.fillMaxSize())
                is ContentState.Error -> ScreenNotLoaded(
                    theme = theme,
                    tryAgain = if (!state.loading) tryAgain else null
                )
            }
        }
        CardCircleProgress(
            theme = theme,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(theme.dimensions.loadingPadding),
            loading = state.loading
        )
    }
}