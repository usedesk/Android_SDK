package ru.usedesk.knowledgebase_sdk.data.repository.api

import androidx.annotation.CheckResult
import ru.usedesk.knowledgebase_sdk.data.repository.api.entity.SearchQueryRequest
import ru.usedesk.knowledgebase_sdk.entity.UsedeskArticleContent
import ru.usedesk.knowledgebase_sdk.entity.UsedeskSection

internal interface IKnowledgeBaseApi {
    @CheckResult
    suspend fun getSections(): GetSectionsResponse

    sealed interface GetSectionsResponse {
        data class Done(val sections: List<UsedeskSection>) : GetSectionsResponse
        data class Error(val code: Int? = null) : GetSectionsResponse
    }

    fun getArticle(articleId: Long): UsedeskArticleContent

    fun getArticles(searchQueryRequest: SearchQueryRequest): List<UsedeskArticleContent>

    fun addViews(articleId: Long)

    fun sendRating(
        articleId: Long,
        good: Boolean
    )

    fun sendRating(
        articleId: Long,
        message: String
    )
}