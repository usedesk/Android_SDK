package ru.usedesk.knowledgebase_gui.screens.articles

import ru.usedesk.common_gui.UsedeskViewModel
import ru.usedesk.knowledgebase_sdk.UsedeskKnowledgeBaseSdk
import ru.usedesk.knowledgebase_sdk.entity.UsedeskArticleInfo

internal class ArticlesViewModel : UsedeskViewModel<ArticlesViewModel.Model>(Model()) {

    fun init(categoryId: Long) {
        doIt(UsedeskKnowledgeBaseSdk.requireInstance()
            .getArticlesRx(categoryId), onValue = {
            setModel { model ->
                model.copy(
                    articleInfoList = it,
                    loading = false
                )
            }
        })
    }

    data class Model(
        val articleInfoList: List<UsedeskArticleInfo> = listOf(),
        val loading: Boolean = true
    )
}