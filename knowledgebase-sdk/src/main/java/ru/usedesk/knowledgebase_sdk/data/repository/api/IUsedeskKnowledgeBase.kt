
package ru.usedesk.knowledgebase_sdk.data.repository.api

import androidx.annotation.CheckResult
import ru.usedesk.knowledgebase_sdk.entity.UsedeskArticleContent
import ru.usedesk.knowledgebase_sdk.entity.UsedeskSection

interface IUsedeskKnowledgeBase {
    @CheckResult
    fun getSections(): GetSectionsResponse

    sealed interface GetSectionsResponse {
        data class Done(val sections: List<UsedeskSection>) : GetSectionsResponse
        data class Error(val code: Int? = null) : GetSectionsResponse
    }

    @CheckResult
    fun getArticle(articleId: Long): GetArticleResponse

    sealed interface GetArticleResponse {
        data class Done(val articleContent: UsedeskArticleContent) : GetArticleResponse
        data class Error(val code: Int? = null) : GetArticleResponse
    }

    @CheckResult
    fun getArticles(
        query: String,
        page: Long
    ): GetArticlesResponse

    sealed interface GetArticlesResponse {
        class Done(val articles: List<UsedeskArticleContent>) : GetArticlesResponse
        class Error(val code: Int? = null) : GetArticlesResponse
    }

    @CheckResult
    fun addViews(articleId: Long): AddViewsResponse

    sealed interface AddViewsResponse {
        class Done(val views: Long) : AddViewsResponse
        class Error(val code: Int? = null) : AddViewsResponse
    }

    @CheckResult
    fun sendRating(
        articleId: Long,
        good: Boolean
    ): SendRatingResponse

    @CheckResult
    fun sendReview(
        subject: String,
        message: String
    ): SendReviewResponse

    sealed interface SendRatingResponse {
        class Done(
            val positive: Int,
            val negative: Int
        ) : SendRatingResponse

        class Error(val code: Int?) : SendRatingResponse
    }

    sealed interface SendReviewResponse {
        class Done : SendReviewResponse
        class Error(val code: Int?) : SendReviewResponse
    }
}