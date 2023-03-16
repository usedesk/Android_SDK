package ru.usedesk.knowledgebase_gui.screens.main.blocks.categories

import ru.usedesk.common_gui.UsedeskViewModel
import ru.usedesk.knowledgebase_gui.screens.main.blocks.categories.CategoriesViewModel.State
import ru.usedesk.knowledgebase_sdk.domain.IUsedeskKnowledgeBase
import ru.usedesk.knowledgebase_sdk.entity.UsedeskCategory

internal class CategoriesViewModel(
    private val knowledgeBase: IUsedeskKnowledgeBase,
    private val sectionId: Long
) : UsedeskViewModel<State>(State()) {

    init {
        knowledgeBase.modelFlow.launchCollect { model ->
            setModel {
                copy(
                    categories = model.sectionsMap
                        ?.get(sectionId)
                        ?.categories
                        ?: categories
                )
            }
        }
    }

    data class State(
        val categories: List<UsedeskCategory> = listOf()
    )
}