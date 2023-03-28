package ru.usedesk.knowledgebase_gui.screen.loading

import ru.usedesk.common_gui.UsedeskViewModel
import ru.usedesk.knowledgebase_gui._entity.LoadingState
import ru.usedesk.knowledgebase_gui.domain.IKnowledgeBaseInteractor
import ru.usedesk.knowledgebase_gui.screen.loading.LoadingViewModel.State

internal class LoadingViewModel(
    private val kbInteractor: IKnowledgeBaseInteractor
) : UsedeskViewModel<State>(State()) {

    init {
        kbInteractor.sectionsModelFlow.launchCollect { sectionsModel ->
            setModel {
                when (sectionsModel.loadingState) {
                    is LoadingState.Loading -> copy(
                        loading = true
                    )
                    is LoadingState.Failed -> copy(
                        error = true,
                        loading = false
                    )
                    is LoadingState.Loaded -> this
                }
            }
        }
    }

    data class State(
        val error: Boolean = false,
        val loading: Boolean = true
    )
}