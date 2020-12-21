package ru.usedesk.knowledgebase_gui.pages.sections

import androidx.lifecycle.MutableLiveData
import ru.usedesk.common_gui.UsedeskViewModel
import ru.usedesk.knowledgebase_sdk.UsedeskKnowledgeBaseSdk
import ru.usedesk.knowledgebase_sdk.entity.UsedeskSection

internal class SectionsViewModel : UsedeskViewModel() {

    val sectionsLiveData = MutableLiveData<List<UsedeskSection>>()

    init {
        doIt(UsedeskKnowledgeBaseSdk.getInstance()
                .getSectionsRx(), onValue = {
            sectionsLiveData.postValue(it)
        })
    }
}