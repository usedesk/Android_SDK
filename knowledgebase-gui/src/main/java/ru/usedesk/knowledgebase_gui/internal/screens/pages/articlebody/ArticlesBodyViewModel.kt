package ru.usedesk.knowledgebase_gui.internal.screens.pages.articlebody

import ru.usedesk.knowledgebase_gui.internal.screens.common.DataViewModel
import ru.usedesk.knowledgebase_gui.internal.screens.common.ViewModelFactory
import ru.usedesk.knowledgebase_sdk.external.IUsedeskKnowledgeBase
import ru.usedesk.knowledgebase_sdk.external.entity.UsedeskArticleBody

class ArticlesBodyViewModel private constructor(private val usedeskKnowledgeBaseSdk: IUsedeskKnowledgeBase, searchQuery: String) : DataViewModel<List<UsedeskArticleBody?>?>() {
    fun onSearchQueryUpdate(searchQuery: String) {
        loadData(usedeskKnowledgeBaseSdk.getArticlesRx(searchQuery))
    }

    internal class Factory(private val usedeskKnowledgeBaseSdk: IUsedeskKnowledgeBase, private val searchQuery: String) : ViewModelFactory<ArticlesBodyViewModel?>() {
        override fun create(): ArticlesBodyViewModel {
            return ArticlesBodyViewModel(usedeskKnowledgeBaseSdk, searchQuery)
        }

        override fun getClassType(): Class<ArticlesBodyViewModel?> {
            return ArticlesBodyViewModel::class.java
        }
    }

    init {
        onSearchQueryUpdate(searchQuery)
    }
}