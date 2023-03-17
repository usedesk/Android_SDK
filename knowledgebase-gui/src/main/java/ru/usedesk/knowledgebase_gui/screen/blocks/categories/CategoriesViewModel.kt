package ru.usedesk.knowledgebase_gui.screen.blocks.categories

import ru.usedesk.common_gui.UsedeskViewModel
import ru.usedesk.knowledgebase_gui.screen.blocks.categories.CategoriesViewModel.State
import ru.usedesk.knowledgebase_sdk.UsedeskKnowledgeBaseSdk
import ru.usedesk.knowledgebase_sdk.entity.UsedeskCategory

internal class CategoriesViewModel(
    private val sectionId: Long
) : UsedeskViewModel<State>(State()) {

    private val knowledgeBase = UsedeskKnowledgeBaseSdk.requireInstance()

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