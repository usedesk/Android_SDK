package ru.usedesk.knowledgebase_sdk.data.repository.api

import com.google.gson.Gson
import ru.usedesk.common_sdk.api.IUsedeskApiFactory
import ru.usedesk.common_sdk.api.UsedeskApiRepository
import ru.usedesk.common_sdk.api.multipart.IUsedeskMultipartConverter
import ru.usedesk.common_sdk.entity.exceptions.UsedeskDataNotFoundException
import ru.usedesk.common_sdk.entity.exceptions.UsedeskHttpException
import ru.usedesk.knowledgebase_sdk.data.repository.api.entity.*
import ru.usedesk.knowledgebase_sdk.entity.*
import javax.inject.Inject

internal class KnowledgeBaseApi @Inject constructor(
    private val configuration: UsedeskKnowledgeBaseConfiguration,
    multipartConverter: IUsedeskMultipartConverter,
    apiFactory: IUsedeskApiFactory,
    gson: Gson
) : UsedeskApiRepository<ApiRetrofit>(
    apiFactory,
    multipartConverter,
    gson,
    ApiRetrofit::class.java
), IKnowledgeBaseApi {

    private var sections: List<UsedeskSection>? = null

    override fun getSections() = sections ?: loadSections().also { sections = it }

    override fun getCategories(sectionId: Long) = getSections().firstOrNull { it.id == sectionId }
        ?.categories
        ?: throw UsedeskDataNotFoundException("Categories not found by section id($sectionId)")

    override fun getArticles(categoryId: Long) = getSections()
        .flatMap(UsedeskSection::categories)
        .firstOrNull { it.id == categoryId }
        ?.articles
        ?: throw UsedeskDataNotFoundException("Articles not found by category id($categoryId)")

    override fun getArticle(articleId: Long): UsedeskArticleContent {
        val articleContentResponse = doRequest(
            configuration.urlApi,
            ArticleContentResponse::class.java
        ) {
            getArticleContent(configuration.accountId, articleId, configuration.token)
        }
        return valueOrNull {
            UsedeskArticleContent(
                articleContentResponse.id!!,
                articleContentResponse.title ?: "",
                articleContentResponse.text ?: "",
                articleContentResponse.categoryId?.toLongOrNull()!!
            )
        } ?: throw UsedeskHttpException(message = "Wrong response")
    }

    override fun getArticles(searchQueryRequest: SearchQueryRequest): List<UsedeskArticleContent> {
        val articlesSearchResponse = doRequest(
            configuration.urlApi,
            ArticlesSearchResponse::class.java
        ) {
            getArticles(
                configuration.accountId,
                configuration.token,
                searchQueryRequest.query,
                searchQueryRequest.count,
                searchQueryRequest.sectionIds?.joinToString(","),
                searchQueryRequest.categoryIds?.joinToString(","),
                searchQueryRequest.articleIds?.joinToString(","),
                searchQueryRequest.page,
                searchQueryRequest.type?.name?.lowercase(),
                searchQueryRequest.sort?.name?.lowercase(),
                searchQueryRequest.order?.name?.lowercase()
            )
        }

        return (articlesSearchResponse.articles ?: arrayOf()).mapNotNull {
            valueOrNull {
                UsedeskArticleContent(
                    it!!.id!!,
                    it.title ?: "",
                    it.text ?: "",
                    it.categoryId?.toLongOrNull()!!
                )
            }
        }
    }

    override fun addViews(articleId: Long) {
        doRequest(
            configuration.urlApi,
            AddViewsResponse::class.java
        ) {
            addViews(
                configuration.accountId,
                articleId,
                configuration.token,
                1
            )
        }
    }

    override fun sendRating(
        articleId: Long,
        good: Boolean
    ) {
        doRequest(
            configuration.urlApi,
            ChangeRatingResponse::class.java
        ) {
            changeRating(
                configuration.accountId,
                articleId,
                if (good) 1 else 0,
                if (good) 0 else 1
            )
        }
    }

    override fun sendRating(
        articleId: Long,
        message: String
    ) {
        doRequest(
            configuration.urlApi,
            CreateTicketResponse::class.java
        ) {
            createTicket(
                CreateTicketRequest(
                    configuration.token,
                    configuration.clientEmail,
                    configuration.clientName,
                    message,
                    articleId
                )
            )
        }
    }

    private fun loadSections(): List<UsedeskSection> {
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
        return response!!.items!!.mapNotNull { sectionResponse ->
            valueOrNull {
                val categories = sectionResponse!!.categories?.mapNotNull { categoryResponse ->
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
                } ?: listOf()
                UsedeskSection(
                    sectionResponse.id!!,
                    sectionResponse.title ?: "",
                    sectionResponse.image,
                    categories
                )
            }
        }
    }
}