package ru.usedesk.knowledgebase_gui.screen.article

import ru.usedesk.common_gui.UsedeskViewModel
import ru.usedesk.knowledgebase_gui.screen.article.ArticleViewModel.State
import ru.usedesk.knowledgebase_sdk.UsedeskKnowledgeBaseSdk
import ru.usedesk.knowledgebase_sdk.domain.IUsedeskKnowledgeBase.GetArticleResult
import ru.usedesk.knowledgebase_sdk.entity.UsedeskArticleContent

internal class ArticleViewModel(
    private val articleId: Long
) : UsedeskViewModel<State>(State()) {

    private val knowledgeBase = UsedeskKnowledgeBaseSdk.requireInstance()

    init {
        loadArticle()
    }

    private fun loadArticle() {
        knowledgeBase.getArticle(articleId) { result ->
            setModel {
                copy(
                    content = when (result) {
                        is GetArticleResult.Done -> State.Content.Article(result.articleContent)
                        is GetArticleResult.Error -> when (content) {
                            is State.Content.Article -> content
                            is State.Content.Loading -> content.copy(
                                loading = false,
                                error = true
                            )
                        }
                    }
                )
            }
        }
    }

    fun tryAgain() {
        loadArticle()
    }

    data class State(
        val content: Content = Content.Loading(),
    ) {
        sealed interface Content {
            data class Loading(
                val loading: Boolean = true,
                val error: Boolean = false
            ) : Content

            data class Article(val articleContent: UsedeskArticleContent) : Content
        }
    }
}