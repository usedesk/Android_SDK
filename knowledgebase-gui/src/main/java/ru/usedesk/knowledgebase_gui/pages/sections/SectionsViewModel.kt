package ru.usedesk.knowledgebase_gui.pages.sections

import ru.usedesk.knowledgebase_gui.common.DataViewModel
import ru.usedesk.knowledgebase_sdk.UsedeskKnowledgeBaseSdk
import ru.usedesk.knowledgebase_sdk.entity.UsedeskSection

internal class SectionsViewModel : DataViewModel<List<UsedeskSection>>() {

    init {
        loadData(UsedeskKnowledgeBaseSdk.getInstance().getSectionsRx())
    }
}