package ru.usedesk.knowledgebase_sdk.data.repository.api

import com.google.gson.Gson
import ru.usedesk.common_sdk.api.IUsedeskApiFactory
import ru.usedesk.common_sdk.api.UsedeskApiRepository
import ru.usedesk.common_sdk.api.multipart.IUsedeskMultipartConverter
import ru.usedesk.knowledgebase_sdk.data.repository.api.IUsedeskKnowledgeBase.*
import ru.usedesk.knowledgebase_sdk.data.repository.api.entity.*
import ru.usedesk.knowledgebase_sdk.entity.*
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
            listOf(
                UsedeskCategory(
                    categoryId,
                    categoryResponse.title ?: "",
                    categoryResponse.description ?: "",
                    articles
                ),
                UsedeskCategory(
                    categoryId + 10000L,
                    categoryResponse.title ?: "",
                    categoryResponse.description ?: "",
                    articles
                ),
                UsedeskCategory(
                    categoryId + 1000000L,
                    categoryResponse.title ?: "",
                    categoryResponse.description ?: "",
                    articles
                ),
                UsedeskCategory(
                    categoryId + 100000000L,
                    categoryResponse.title ?: "",
                    categoryResponse.description ?: "",
                    articles
                ),
                UsedeskCategory(
                    categoryId + 10000000000L,
                    categoryResponse.title ?: "",
                    categoryResponse.description ?: "",
                    articles
                )
            )
        }
    }.flatten()

    private fun Array<SectionResponse?>.convert() = mapNotNull { sectionResponse ->
        valueOrNull {
            val categories = sectionResponse!!.categories?.convert() ?: listOf()
            listOf(
                UsedeskSection(
                    sectionResponse.id!!,
                    sectionResponse.title ?: "",
                    sectionResponse.image,
                    categories
                ),
                UsedeskSection(
                    sectionResponse.id!! + 10000L,
                    sectionResponse.title ?: "",
                    sectionResponse.image,
                    categories
                ),
                UsedeskSection(
                    sectionResponse.id!! + 100000000L,
                    sectionResponse.title ?: "",
                    sectionResponse.image,
                    categories
                ),
                UsedeskSection(
                    sectionResponse.id!! + 10000000000L,
                    sectionResponse.title ?: "",
                    sectionResponse.image,
                    categories
                ),
                UsedeskSection(
                    sectionResponse.id!! + 1000000000000L,
                    sectionResponse.title ?: "",
                    sectionResponse.image,
                    categories
                ),
                UsedeskSection(
                    sectionResponse.id!! + 100000000000000L,
                    sectionResponse.title ?: "",
                    sectionResponse.image,
                    categories
                )
            )
        }
    }.flatten()

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
            getSections(it.accountId, it.token)
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

    override fun getArticles(query: String): GetArticlesResponse {
        val request = GetArticles.Request(
            query = query
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
                request.sectionIds?.joinToString(","),
                request.categoryIds?.joinToString(","),
                request.articleIds?.joinToString(","),
                request.page,
                request.type?.name?.lowercase(),
                request.sort?.name?.lowercase(),
                request.order?.name?.lowercase()
            )
        }

        return when (response?.articles) {
            null -> GetArticlesResponse.Error(response?.code)
            else -> GetArticlesResponse.Done(response.articles.mapNotNull { it?.convert() })
        }
    }

    private fun GetArticleContent.Response.convert() = valueOrNull {
        UsedeskArticleContent(
            id!!,
            title ?: "",
            categoryId?.toLongOrNull()!!,
            views ?: 0,
            text ?: ""
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
                request.token,
                request.count
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
    ): SendResponse {
        val request = AddRating.Request(
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
                positive = request.positive,
                negative = request.negative
            )
        }
        return when (response?.rating) {
            null -> SendResponse.Error(response?.code)
            else -> SendResponse.Done
        }
    }

    override fun sendReview(
        articleId: Long,
        message: String
    ): SendResponse {
        val request = CreateTicket.Request(
            configuration.token,
            configuration.clientEmail,
            configuration.clientName,
            message,
            articleId
        )
        val response = doRequestJson(
            configuration.urlApi,
            request,
            CreateTicket.Response::class.java
        ) {
            createTicket(request)
        }
        return when (response?.status) {
            "success" -> SendResponse.Done
            else -> SendResponse.Error(response?.code)
        }
    }
}