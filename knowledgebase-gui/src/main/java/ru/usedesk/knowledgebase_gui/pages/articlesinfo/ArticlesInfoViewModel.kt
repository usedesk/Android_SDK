package ru.usedesk.knowledgebase_gui.pages.articlesinfo

import ru.usedesk.knowledgebase_gui.common.DataViewModel
import ru.usedesk.knowledgebase_sdk.UsedeskKnowledgeBaseSdk
import ru.usedesk.knowledgebase_sdk.entity.UsedeskArticleInfo

internal class ArticlesInfoViewModel : DataViewModel<List<UsedeskArticleInfo>>() {

    fun init(categoryId: Long) {
        loadData(UsedeskKnowledgeBaseSdk.getInstance().getArticlesRx(categoryId))
    }
}