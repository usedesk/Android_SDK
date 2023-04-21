
package ru.usedesk.knowledgebase_sdk.data.repository.api

import com.google.gson.Gson
import ru.usedesk.common_sdk.api.IUsedeskApiFactory
import ru.usedesk.common_sdk.api.UsedeskApiRepository
import ru.usedesk.common_sdk.api.multipart.IUsedeskMultipartConverter
import ru.usedesk.knowledgebase_sdk.data.repository.api.IUsedeskKnowledgeBase.AddViewsResponse
import ru.usedesk.knowledgebase_sdk.data.repository.api.IUsedeskKnowledgeBase.GetArticleResponse
import ru.usedesk.knowledgebase_sdk.data.repository.api.IUsedeskKnowledgeBase.GetArticlesResponse
import ru.usedesk.knowledgebase_sdk.data.repository.api.IUsedeskKnowledgeBase.GetSectionsResponse
import ru.usedesk.knowledgebase_sdk.data.repository.api.IUsedeskKnowledgeBase.SendRatingResponse
import ru.usedesk.knowledgebase_sdk.data.repository.api.IUsedeskKnowledgeBase.SendReviewResponse
import ru.usedesk.knowledgebase_sdk.data.repository.api.entity.AddRating
import ru.usedesk.knowledgebase_sdk.data.repository.api.entity.AddViews
import ru.usedesk.knowledgebase_sdk.data.repository.api.entity.CategoryResponse
import ru.usedesk.knowledgebase_sdk.data.repository.api.entity.CreateTicket
import ru.usedesk.knowledgebase_sdk.data.repository.api.entity.GetArticleContent
import ru.usedesk.knowledgebase_sdk.data.repository.api.entity.GetArticles
import ru.usedesk.knowledgebase_sdk.data.repository.api.entity.LoadSections
import ru.usedesk.knowledgebase_sdk.data.repository.api.entity.SectionResponse
import ru.usedesk.knowledgebase_sdk.entity.UsedeskArticleContent
import ru.usedesk.knowledgebase_sdk.entity.UsedeskArticleInfo
import ru.usedesk.knowledgebase_sdk.entity.UsedeskCategory
import ru.usedesk.knowledgebase_sdk.entity.UsedeskKnowledgeBaseConfiguration
import ru.usedesk.knowledgebase_sdk.entity.UsedeskSection
import javax.inject.Inject

internal class KbRepository @Inject constructor(
    private val configuration: UsedeskKnowledgeBaseConfiguration,
    multipartConverter: IUsedeskMultipartConverter,
    apiFactory: IUsedeskApiFactory,
    gson: Gson
) : UsedeskApiRepository<ApiRetrofit>(
    apiFactory,
    multipartConverter,
    gson,
    ApiRetrofit::class.java
), IUsedeskKnowledgeBase {

    private fun Array<CategoryResponse?>.convert() = mapNotNull { categoryResponse ->
        valueOrNull {
            val categoryId = categoryResponse!!.id!!
            val articles = categoryResponse.articles?.mapNotNull { articleResponse ->
                valueOrNull {
                    UsedeskArticleInfo(
                        articleResponse!!.id!!,
                        articleResponse.title ?: "",
                        categoryId,
                        articleResponse.views ?: 0
                    )
                }
            } ?: listOf()

            UsedeskCategory(
                categoryId,
                categoryResponse.title ?: "",
                categoryResponse.description ?: "",
                articles
            )
        }
    }

    private fun Array<SectionResponse?>.convert() = mapNotNull { sectionResponse ->
        valueOrNull {
            val categories = sectionResponse!!.categories?.convert() ?: listOf()
            UsedeskSection(
                sectionResponse.id!!,
                sectionResponse.title ?: "",
                sectionResponse.image,
                categories
            )
        }
    }

    override fun getSections(): GetSectionsResponse {
        val request = LoadSections.Request(
            configuration.token,
            configuration.accountId
        )
        val response = doRequestJson(
            configuration.urlApi,
            request,
            LoadSections.Response::class.java
        ) {
            getSections(it.accountId, it.apiToken)
        }
        return when (response?.items) {
            null -> GetSectionsResponse.Error(response?.code)
            else -> GetSectionsResponse.Done(response.items.convert())
        }
    }

    override fun getArticle(articleId: Long): GetArticleResponse {
        val request = GetArticleContent.Request(
            configuration.accountId,
            articleId,
            configuration.token
        )
        val response = doRequestJson(
            configuration.urlApi,
            request,
            GetArticleContent.Response::class.java
        ) {
            getArticleContent(
                it.accountId,
                it.articleId,
                it.token
            )
        }
        return when (val articleContent = response?.convert()) {
            null -> GetArticleResponse.Error(response?.code)
            else -> GetArticleResponse.Done(articleContent)
        }
    }

    override fun getArticles(
        query: String,
        page: Long
    ): GetArticlesResponse {
        val request = GetArticles.Request(
            query = query,
            page = page,
            type = GetArticles.Request.Type.PUBLIC
        )
        val response = doRequestJson(
            configuration.urlApi,
            request,
            GetArticles.Response::class.java
        ) { request ->
            getArticles(
                configuration.accountId,
                configuration.token,
                request.query,
                request.count,
                request.sectionIds,
                request.categoryIds,
                request.articleIds,
                request.page,
                request.type,
                request.sort,
                request.order
            )
        }

        return when (response?.articles) {
            null -> GetArticlesResponse.Error(response?.code)
            else -> GetArticlesResponse.Done(response.articles.mapNotNull { it?.convert() })
        }
    }

    private fun GetArticleContent.Response.convert() = valueOrNull {
        UsedeskArticleContent(
            id = id!!,
            title = title ?: "",
            categoryId = categoryId?.toLongOrNull()!!,
            viewsCount = views ?: 0,
            text = text ?: "",
            public = public == 1
        )
    }

    override fun addViews(articleId: Long): AddViewsResponse {
        val request = AddViews.Request(
            configuration.token,
            configuration.accountId,
            articleId
        )
        val response = doRequestJson(
            configuration.urlApi,
            request,
            AddViews.Response::class.java
        ) { request ->
            addViews(
                request.accountId,
                request.articleId,
                request
            )
        }
        return when (response?.views) {
            null -> AddViewsResponse.Error(response?.code)
            else -> AddViewsResponse.Done(response.views)
        }
    }

    override fun sendRating(
        articleId: Long,
        good: Boolean
    ): SendRatingResponse {
        val request = AddRating.Request(
            configuration.token,
            configuration.accountId,
            articleId,
            if (good) 1 else 0,
            if (good) 0 else 1
        )
        val response = doRequestJson(
            configuration.urlApi,
            request,
            AddRating.Response::class.java
        ) {
            changeRating(
                accountId = request.accountId,
                articleId = request.articleId,
                body = request,
            )
        }
        return when (response?.rating) {
            null -> SendRatingResponse.Error(response?.code)
            else -> SendRatingResponse.Done(
                response.rating.positive ?: 0,
                response.rating.positive ?: 0
            )
        }
    }

    override fun sendReview(
        subject: String,
        message: String
    ): SendReviewResponse {
        val request = CreateTicket.Request(
            configuration.token,
            configuration.clientEmail,
            configuration.clientName,
            subject,
            message
        )
        val response = doRequestJson(
            configuration.urlApi,
            request,
            CreateTicket.Response::class.java,
            ApiRetrofit::createTicket
        )
        return when (response?.status) {
            "success" -> SendReviewResponse.Done()
            else -> SendReviewResponse.Error(response?.code)
        }
    }
}