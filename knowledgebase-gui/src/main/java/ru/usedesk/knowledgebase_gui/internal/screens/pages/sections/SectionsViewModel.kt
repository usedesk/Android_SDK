package ru.usedesk.knowledgebase_gui.internal.screens.pages.sections

import ru.usedesk.knowledgebase_gui.internal.screens.common.DataViewModel
import ru.usedesk.knowledgebase_sdk.external.UsedeskKnowledgeBaseSdk
import ru.usedesk.knowledgebase_sdk.external.entity.UsedeskSection

internal class SectionsViewModel : DataViewModel<List<UsedeskSection>>() {

    init {
        loadData(UsedeskKnowledgeBaseSdk.getInstance().getSectionsRx())
    }
}