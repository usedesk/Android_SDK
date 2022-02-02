package ru.usedesk.knowledgebase_gui.screens.sections

import ru.usedesk.common_gui.UsedeskViewModel
import ru.usedesk.knowledgebase_sdk.UsedeskKnowledgeBaseSdk
import ru.usedesk.knowledgebase_sdk.entity.UsedeskSection

internal class SectionsViewModel : UsedeskViewModel<SectionsViewModel.Model>(Model()) {

    init {
        doIt(UsedeskKnowledgeBaseSdk.requireInstance()
            .getSectionsRx(), {
            setModel { model ->
                model.copy(
                    sections = it,
                    loading = false
                )
            }
        })
    }

    data class Model(
        val sections: List<UsedeskSection> = listOf(),
        val loading: Boolean = true
    )
}