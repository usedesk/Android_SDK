package ru.usedesk.knowledgebase_gui.screens.main

import androidx.lifecycle.MutableLiveData
import ru.usedesk.common_gui.UsedeskViewModel
import ru.usedesk.knowledgebase_sdk.UsedeskKnowledgeBaseSdk.release

internal class KnowledgeBaseViewModel : UsedeskViewModel() {

    val searchQueryLiveData = MutableLiveData<String>()
    private val delayedQuery: DelayedQuery = DelayedQuery(searchQueryLiveData, SEARCH_DELAY)

    override fun onCleared() {
        super.onCleared()
        delayedQuery.dispose()
        release()
    }

    fun onSearchQuery(query: String) {
        delayedQuery.onNext(query)
    }

    companion object {
        private const val SEARCH_DELAY = 500
    }
}