package ru.usedesk.knowledgebase_gui.screens.main

import ru.usedesk.common_gui.UsedeskViewModel
import ru.usedesk.knowledgebase_sdk.UsedeskKnowledgeBaseSdk

internal class KnowledgeBaseViewModel : UsedeskViewModel<KnowledgeBaseViewModel.Model>(Model()) {

    fun init(onInit: () -> Unit) {
        doInit {
            onInit()
        }
    }

    fun onSearchQuery(query: String) {
        setModel { copy(searchQuery = query) }
    }

    override fun onCleared() {
        super.onCleared()

        UsedeskKnowledgeBaseSdk.release()
    }

    data class Model(
        val searchQuery: String = ""
    )
}