package ru.usedesk.knowledgebase_gui.screens.main.blocks.articles

import ru.usedesk.common_gui.UsedeskViewModel
import ru.usedesk.knowledgebase_gui.screens.main.blocks.articles.ArticlesViewModel.State
import ru.usedesk.knowledgebase_sdk.UsedeskKnowledgeBaseSdk
import ru.usedesk.knowledgebase_sdk.entity.UsedeskArticleInfo

internal class ArticlesViewModel(
    private val categoryId: Long
) : UsedeskViewModel<State>(State()) {

    private val knowledgeBase = UsedeskKnowledgeBaseSdk.requireInstance()

    init {
        knowledgeBase.modelFlow.launchCollect { model ->
            setModel {
                copy(
                    articles = model.categoriesMap
                        ?.get(categoryId)
                        ?.articles
                        ?: articles
                )
            }
        }
    }


    data class State(
        val articles: List<UsedeskArticleInfo> = listOf()
    )
}