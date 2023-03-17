package ru.usedesk.knowledgebase_gui.screen.blocks.sections

import ru.usedesk.common_gui.UsedeskViewModel
import ru.usedesk.knowledgebase_gui.screen.blocks.sections.SectionsViewModel.State
import ru.usedesk.knowledgebase_sdk.UsedeskKnowledgeBaseSdk
import ru.usedesk.knowledgebase_sdk.entity.UsedeskSection

internal class SectionsViewModel : UsedeskViewModel<State>(State()) {

    private val knowledgeBase = UsedeskKnowledgeBaseSdk.requireInstance()

    init {
        knowledgeBase.modelFlow.launchCollect { model ->
            setModel { copy(sections = model.sections ?: sections) }
        }
    }

    data class State(
        val sections: List<UsedeskSection> = listOf()
    )
}