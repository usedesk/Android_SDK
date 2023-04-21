
package ru.usedesk.knowledgebase_gui.screen.compose.loading

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ru.usedesk.knowledgebase_gui._entity.ContentState
import ru.usedesk.knowledgebase_gui.compose.CardCircleProgress
import ru.usedesk.knowledgebase_gui.compose.ScreenNotLoaded
import ru.usedesk.knowledgebase_gui.compose.StoreKeys
import ru.usedesk.knowledgebase_gui.compose.ViewModelStoreFactory
import ru.usedesk.knowledgebase_gui.compose.kbUiViewModel
import ru.usedesk.knowledgebase_gui.compose.padding
import ru.usedesk.knowledgebase_gui.compose.rememberViewModelStoreOwner
import ru.usedesk.knowledgebase_gui.screen.RootViewModel
import ru.usedesk.knowledgebase_gui.screen.UsedeskKnowledgeBaseTheme

@Composable
internal fun ContentLoading(
    theme: UsedeskKnowledgeBaseTheme,
    getCurrentScreen: () -> RootViewModel.State.Screen,
    viewModelStoreFactory: ViewModelStoreFactory,
    tryAgain: () -> Unit
) {
    val viewModel = kbUiViewModel(
        viewModelStoreOwner = rememberViewModelStoreOwner {
            viewModelStoreFactory.get(StoreKeys.LOADING.name)
        }
    ) { kbUiComponent -> LoadingViewModel(kbUiComponent.interactor) }
    DisposableEffect(Unit) {
        onDispose {
            if (getCurrentScreen() !is RootViewModel.State.Screen.Loading) {
                viewModelStoreFactory.clear(StoreKeys.LOADING.name)
            }
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
                    tryAgain = tryAgain,
                    tryAgainVisible = !state.loading
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