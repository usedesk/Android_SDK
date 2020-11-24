package ru.usedesk.knowledgebase_gui.internal.screens.pages.sections

import ru.usedesk.knowledgebase_gui.internal.screens.common.DataViewModel
import ru.usedesk.knowledgebase_gui.internal.screens.common.ViewModelFactory
import ru.usedesk.knowledgebase_sdk.external.IUsedeskKnowledgeBase
import ru.usedesk.knowledgebase_sdk.external.entity.UsedeskSection

class SectionsViewModel private constructor(usedeskKnowledgeBaseSdk: IUsedeskKnowledgeBase) : DataViewModel<List<UsedeskSection?>?>() {
    internal class Factory(private val usedeskKnowledgeBaseSdk: IUsedeskKnowledgeBase) : ViewModelFactory<SectionsViewModel?>() {
        override fun create(): SectionsViewModel {
            return SectionsViewModel(usedeskKnowledgeBaseSdk)
        }

        override fun getClassType(): Class<SectionsViewModel?> {
            return SectionsViewModel::class.java
        }
    }

    init {
        loadData(usedeskKnowledgeBaseSdk.sectionsRx)
    }
}