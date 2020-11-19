package ru.usedesk.knowledgebase_gui.internal.screens.pages.articlebody

import ru.usedesk.knowledgebase_gui.internal.screens.common.DataViewModel
import ru.usedesk.knowledgebase_sdk.external.UsedeskKnowledgeBaseSdk
import ru.usedesk.knowledgebase_sdk.external.entity.UsedeskArticleBody

internal class ArticlesBodyViewModel : DataViewModel<List<UsedeskArticleBody>>() {

    fun init(searchQuery: String) {
        onSearchQueryUpdate(searchQuery)
    }

    fun onSearchQueryUpdate(searchQuery: String) {
        loadData(UsedeskKnowledgeBaseSdk.getInstance().getArticlesRx(searchQuery))
    }
}