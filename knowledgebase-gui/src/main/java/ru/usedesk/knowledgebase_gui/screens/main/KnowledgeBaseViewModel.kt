package ru.usedesk.knowledgebase_gui.screens.main

import androidx.lifecycle.MutableLiveData
import ru.usedesk.common_gui.UsedeskViewModel
import ru.usedesk.knowledgebase_sdk.UsedeskKnowledgeBaseSdk.release

internal class KnowledgeBaseViewModel : UsedeskViewModel() {

    val searchQueryLiveData = MutableLiveData<String?>()

    override fun onCleared() {
        super.onCleared()

        release()
    }

    fun onSearchQuery(query: String) {
        searchQueryLiveData.value = query
    }
}