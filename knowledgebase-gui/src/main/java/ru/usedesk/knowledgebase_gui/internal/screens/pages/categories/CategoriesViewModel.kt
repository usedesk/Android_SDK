package ru.usedesk.knowledgebase_gui.internal.screens.pages.categories

import ru.usedesk.knowledgebase_gui.internal.screens.common.DataViewModel
import ru.usedesk.knowledgebase_gui.internal.screens.common.ViewModelFactory
import ru.usedesk.knowledgebase_sdk.external.IUsedeskKnowledgeBase
import ru.usedesk.knowledgebase_sdk.external.entity.UsedeskCategory

class CategoriesViewModel private constructor(usedeskKnowledgeBaseSdk: IUsedeskKnowledgeBase, sectionId: Long) : DataViewModel<List<UsedeskCategory?>?>() {
    internal class Factory(private val usedeskKnowledgeBaseSdk: IUsedeskKnowledgeBase, private val sectionId: Long) : ViewModelFactory<CategoriesViewModel?>() {
        override fun create(): CategoriesViewModel {
            return CategoriesViewModel(usedeskKnowledgeBaseSdk, sectionId)
        }

        override fun getClassType(): Class<CategoriesViewModel?> {
            return CategoriesViewModel::class.java
        }
    }

    init {
        loadData(usedeskKnowledgeBaseSdk.getCategoriesRx(sectionId))
    }
}