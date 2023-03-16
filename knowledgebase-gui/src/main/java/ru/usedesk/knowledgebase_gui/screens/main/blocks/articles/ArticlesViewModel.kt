package ru.usedesk.knowledgebase_gui.screens.main.blocks.articles

import ru.usedesk.common_gui.UsedeskViewModel
import ru.usedesk.knowledgebase_gui.screens.main.blocks.articles.ArticlesViewModel.State
import ru.usedesk.knowledgebase_sdk.domain.IUsedeskKnowledgeBase
import ru.usedesk.knowledgebase_sdk.entity.UsedeskArticleInfo

internal class ArticlesViewModel(
    private val knowledgeBase: IUsedeskKnowledgeBase,
    private val categoryId: Long
) : UsedeskViewModel<State>(State()) {

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