package ru.usedesk.knowledgebase_gui.pages.categories

import ru.usedesk.knowledgebase_gui.common.DataViewModel
import ru.usedesk.knowledgebase_sdk.UsedeskKnowledgeBaseSdk
import ru.usedesk.knowledgebase_sdk.data.repository.entity.UsedeskCategoryOld

internal class CategoriesViewModel : DataViewModel<List<UsedeskCategoryOld>>() {

    fun init(sectionId: Long) {
        loadData(UsedeskKnowledgeBaseSdk.getInstance().getCategoriesRx(sectionId))
    }
}