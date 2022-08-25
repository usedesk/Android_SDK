package ru.usedesk.knowledgebase_gui.screens.categories

import ru.usedesk.common_gui.UsedeskViewModel
import ru.usedesk.knowledgebase_sdk.UsedeskKnowledgeBaseSdk
import ru.usedesk.knowledgebase_sdk.entity.UsedeskCategory

internal class CategoriesViewModel : UsedeskViewModel<CategoriesViewModel.Model>(Model()) {

    fun init(sectionId: Long) {
        doIt(UsedeskKnowledgeBaseSdk.requireInstance()
            .getCategoriesRx(sectionId), {
            setModel {
                copy(
                    categories = it,
                    loading = false
                )
            }
        })
    }

    data class Model(
        val categories: List<UsedeskCategory> = listOf(),
        val loading: Boolean = true
    )
}