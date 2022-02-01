package ru.usedesk.knowledgebase_gui.screens.main

import ru.usedesk.common_gui.UsedeskViewModel
import ru.usedesk.knowledgebase_sdk.UsedeskKnowledgeBaseSdk.release

internal class KnowledgeBaseViewModel : UsedeskViewModel<KnowledgeBaseViewModel.Model>(Model()) {

    fun onSearchQuery(query: String?) {
        setModel { model ->
            model.copy(
                searchQuery = query ?: "",
                showSearch = query != null
            )
        }
    }

    override fun onCleared() {
        super.onCleared()

        release()
    }

    data class Model(
        val searchQuery: String = "",
        val showSearch: Boolean = false
    )
}