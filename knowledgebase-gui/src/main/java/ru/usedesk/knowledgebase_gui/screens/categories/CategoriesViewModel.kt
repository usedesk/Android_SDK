package ru.usedesk.knowledgebase_gui.screens.categories

import androidx.lifecycle.MutableLiveData
import ru.usedesk.common_gui.UsedeskViewModel
import ru.usedesk.knowledgebase_sdk.UsedeskKnowledgeBaseSdk
import ru.usedesk.knowledgebase_sdk.entity.UsedeskCategory

internal class CategoriesViewModel : UsedeskViewModel() {

    val categoriesLiveData = MutableLiveData<List<UsedeskCategory>>()

    fun init(sectionId: Long) {
        doIt(UsedeskKnowledgeBaseSdk.getInstance()
                .getCategoriesRx(sectionId), onValue = {
            categoriesLiveData.postValue(it)
        })
    }
}