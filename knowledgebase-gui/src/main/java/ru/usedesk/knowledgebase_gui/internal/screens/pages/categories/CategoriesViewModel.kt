package ru.usedesk.knowledgebase_gui.internal.screens.pages.categories

import ru.usedesk.knowledgebase_gui.internal.screens.common.DataViewModel
import ru.usedesk.knowledgebase_sdk.external.UsedeskKnowledgeBaseSdk
import ru.usedesk.knowledgebase_sdk.external.entity.UsedeskCategory

internal class CategoriesViewModel : DataViewModel<List<UsedeskCategory>>() {

    fun init(sectionId: Long) {
        loadData(UsedeskKnowledgeBaseSdk.getInstance().getCategoriesRx(sectionId))
    }
}