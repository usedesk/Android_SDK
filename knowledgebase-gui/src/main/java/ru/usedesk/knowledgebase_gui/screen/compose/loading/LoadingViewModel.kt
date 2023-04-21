
package ru.usedesk.knowledgebase_gui.screen.compose.loading

import ru.usedesk.common_gui.UsedeskViewModel
import ru.usedesk.knowledgebase_gui._entity.ContentState
import ru.usedesk.knowledgebase_gui._entity.LoadingState
import ru.usedesk.knowledgebase_gui.domain.IKnowledgeBaseInteractor
import ru.usedesk.knowledgebase_gui.domain.IKnowledgeBaseInteractor.SectionsModel
import ru.usedesk.knowledgebase_gui.screen.compose.loading.LoadingViewModel.State

internal class LoadingViewModel(
    private val kbInteractor: IKnowledgeBaseInteractor
) : UsedeskViewModel<State>(State()) {

    init {
        kbInteractor.loadSections(true).launchCollect { sectionsModel ->
            setModel {
                copy(
                    contentState = contentState.update(
                        loadingState = sectionsModel.loadingState,
                        convert = { this }
                    ),
                    loading = sectionsModel.loadingState !is LoadingState.Error
                )
            }
        }
    }

    data class State(
        val contentState: ContentState<SectionsModel.Data> = ContentState.Empty(),
        val loading: Boolean = true
    )
}