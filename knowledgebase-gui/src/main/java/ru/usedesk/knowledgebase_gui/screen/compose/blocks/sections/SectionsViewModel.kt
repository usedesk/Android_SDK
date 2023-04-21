
package ru.usedesk.knowledgebase_gui.screen.compose.blocks.sections

import androidx.compose.foundation.lazy.LazyListState
import ru.usedesk.common_gui.UsedeskViewModel
import ru.usedesk.knowledgebase_gui._entity.LoadingState
import ru.usedesk.knowledgebase_gui.domain.IKnowledgeBaseInteractor
import ru.usedesk.knowledgebase_gui.screen.compose.blocks.sections.SectionsViewModel.State
import ru.usedesk.knowledgebase_sdk.entity.UsedeskSection

internal class SectionsViewModel(
    private val kbInteractor: IKnowledgeBaseInteractor
) : UsedeskViewModel<State>(State()) {

    init {
        kbInteractor.loadSections().launchCollect { sectionsModel ->
            setModel {
                copy(
                    sections = (sectionsModel.loadingState as? LoadingState.Loaded)
                        ?.data
                        ?.sections
                        ?: listOf()
                )
            }
        }
    }

    data class State(
        val lazyListState: LazyListState = LazyListState(),
        val sections: List<UsedeskSection> = listOf()
    )
}