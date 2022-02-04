package ru.usedesk.knowledgebase_gui.screens.article

import ru.usedesk.common_gui.UsedeskViewModel
import ru.usedesk.knowledgebase_sdk.UsedeskKnowledgeBaseSdk
import ru.usedesk.knowledgebase_sdk.entity.UsedeskArticleInfo

internal class ArticlePageViewModel : UsedeskViewModel<ArticlePageViewModel.Model>(Model()) {

    fun init(
        categoryId: Long,
        articleId: Long
    ) {
        doInit {
            doIt(UsedeskKnowledgeBaseSdk.requireInstance()
                .getArticlesRx(categoryId), { articles ->
                var position = articles.indexOfFirst {
                    it.id == articleId
                }
                if (position < 0) {
                    position = 0
                }
                setModel { model ->
                    model.copy(
                        articles = articles,
                        selectedPosition = position,
                        selectedArticle = articles[position]
                    )
                }
            })
        }
    }

    fun onSelect(position: Int) {
        setModel { model ->
            model.copy(
                selectedPosition = position,
                selectedArticle = model.articles.getOrNull(position)
            )
        }
    }

    data class Model(
        val articles: List<UsedeskArticleInfo> = listOf(),
        val selectedPosition: Int = 0,
        val selectedArticle: UsedeskArticleInfo? = null
    )
}