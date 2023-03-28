package ru.usedesk.knowledgebase_gui.screen.loading

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.usedesk.knowledgebase_gui.compose.ViewModelStoreFactory
import ru.usedesk.knowledgebase_gui.compose.kbUiViewModel
import ru.usedesk.knowledgebase_gui.screen.blocks.search.ScreenNotLoaded

internal const val LOADING_KEY = "article"

@Composable
internal fun ContentLoading(
    viewModelStoreFactory: ViewModelStoreFactory,
    tryAgain: () -> Unit
) {
    val viewModel = kbUiViewModel(
        viewModelStoreOwner = remember { { viewModelStoreFactory.get(LOADING_KEY) } }
    ) { kbUiComponent -> LoadingViewModel(kbUiComponent.interactor) }
    val state by viewModel.modelFlow.collectAsState()
    when {
        state.error -> ScreenNotLoaded(
            loading = state.loading,
            tryAgain = tryAgain
        )
        else -> Box(modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.Center)
            )
        }
    }
}