package ru.usedesk.knowledgebase_gui.internal.screens.pages.articlesinfo

import ru.usedesk.knowledgebase_gui.internal.screens.common.DataViewModel
import ru.usedesk.knowledgebase_sdk.external.UsedeskKnowledgeBaseSdk
import ru.usedesk.knowledgebase_sdk.external.entity.UsedeskArticleInfo

internal class ArticlesInfoViewModel : DataViewModel<List<UsedeskArticleInfo>>() {

    fun init(categoryId: Long) {
        loadData(UsedeskKnowledgeBaseSdk.getInstance().getArticlesRx(categoryId))
    }
}