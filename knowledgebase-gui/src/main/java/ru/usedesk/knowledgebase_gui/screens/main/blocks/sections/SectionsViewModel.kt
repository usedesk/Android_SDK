package ru.usedesk.knowledgebase_gui.screens.main.blocks.sections

import ru.usedesk.common_gui.UsedeskViewModel
import ru.usedesk.knowledgebase_gui.screens.main.blocks.sections.SectionsViewModel.State
import ru.usedesk.knowledgebase_sdk.domain.IUsedeskKnowledgeBase
import ru.usedesk.knowledgebase_sdk.entity.UsedeskSection

internal class SectionsViewModel(
    private val knowledgeBase: IUsedeskKnowledgeBase
) : UsedeskViewModel<State>(State()) {

    init {
        knowledgeBase.modelFlow.launchCollect { model ->
            setModel { copy(sections = model.sections ?: sections) }
        }
    }

    data class State(
        val sections: List<UsedeskSection> = listOf()
    )
}