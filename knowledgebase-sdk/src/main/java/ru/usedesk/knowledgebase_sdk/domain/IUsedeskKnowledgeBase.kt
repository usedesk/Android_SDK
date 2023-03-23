package ru.usedesk.knowledgebase_sdk.domain

import kotlinx.coroutines.flow.StateFlow
import ru.usedesk.knowledgebase_sdk.entity.UsedeskArticleContent
import ru.usedesk.knowledgebase_sdk.entity.UsedeskArticleInfo
import ru.usedesk.knowledgebase_sdk.entity.UsedeskCategory
import ru.usedesk.knowledgebase_sdk.entity.UsedeskSection

interface IUsedeskKnowledgeBase {
    val modelFlow: StateFlow<Model>

    fun loadSections(reload: Boolean = false)

    fun getArticle(
        articleId: Long,
        onResult: (result: GetArticleResult) -> Unit
    )

    sealed interface GetArticleResult {
        data class Done(val articleContent: UsedeskArticleContent) : GetArticleResult
        data class Error(val code: Int? = null) : GetArticleResult
    }

    fun addViews(articleId: Long)

    fun sendRating(
        articleId: Long,
        good: Boolean,
        onResult: (result: SendResult) -> Unit
    )

    fun sendReview(
        articleId: Long,
        message: String,
        onResult: (result: SendResult) -> Unit
    )

    sealed interface SendResult {
        object Done : SendResult
        data class Error(val code: Int? = null) : SendResult
    }

    data class Model(
        val state: State = State.LOADING,
        val sections: List<UsedeskSection>? = null,
        val sectionsMap: Map<Long, UsedeskSection>? = null,
        val categoriesMap: Map<Long, UsedeskCategory>? = null,
        val articlesMap: Map<Long, UsedeskArticleInfo>? = null
    ) {
        enum class State {
            LOADING,
            LOADED,
            FAILED
        }
    }
}