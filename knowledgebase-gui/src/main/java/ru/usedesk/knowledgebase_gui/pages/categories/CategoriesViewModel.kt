package ru.usedesk.knowledgebase_gui.pages.categories

import ru.usedesk.knowledgebase_gui.common.DataViewModel
import ru.usedesk.knowledgebase_sdk.UsedeskKnowledgeBaseSdk
import ru.usedesk.knowledgebase_sdk.entity.UsedeskCategory

internal class CategoriesViewModel : DataViewModel<List<UsedeskCategory>>() {

    fun init(sectionId: Long) {
        loadData(UsedeskKnowledgeBaseSdk.getInstance().getCategoriesRx(sectionId))
    }
}