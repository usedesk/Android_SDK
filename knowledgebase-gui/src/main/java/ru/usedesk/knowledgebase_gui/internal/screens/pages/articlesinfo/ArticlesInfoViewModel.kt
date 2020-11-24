package ru.usedesk.knowledgebase_gui.internal.screens.pages.articlesinfo

import ru.usedesk.knowledgebase_gui.internal.screens.common.DataViewModel
import ru.usedesk.knowledgebase_gui.internal.screens.common.ViewModelFactory
import ru.usedesk.knowledgebase_sdk.external.IUsedeskKnowledgeBase
import ru.usedesk.knowledgebase_sdk.external.entity.UsedeskArticleInfo

class ArticlesInfoViewModel private constructor(usedeskKnowledgeBaseSdk: IUsedeskKnowledgeBase, categoryId: Long) : DataViewModel<List<UsedeskArticleInfo?>?>() {
    internal class Factory(private val usedeskKnowledgeBaseSdk: IUsedeskKnowledgeBase, private val categoryId: Long) : ViewModelFactory<ArticlesInfoViewModel?>() {
        override fun create(): ArticlesInfoViewModel {
            return ArticlesInfoViewModel(usedeskKnowledgeBaseSdk, categoryId)
        }

        override fun getClassType(): Class<ArticlesInfoViewModel?> {
            return ArticlesInfoViewModel::class.java
        }
    }

    init {
        loadData(usedeskKnowledgeBaseSdk.getArticlesRx(categoryId))
    }
}