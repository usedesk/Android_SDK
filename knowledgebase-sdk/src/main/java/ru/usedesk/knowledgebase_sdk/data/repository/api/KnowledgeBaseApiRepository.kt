package ru.usedesk.knowledgebase_sdk.data.repository.api

import com.google.gson.Gson
import ru.usedesk.common_sdk.api.IUsedeskApiFactory
import ru.usedesk.common_sdk.api.UsedeskApiRepository
import ru.usedesk.common_sdk.entity.exceptions.UsedeskDataNotFoundException
import ru.usedesk.common_sdk.entity.exceptions.UsedeskHttpException
import ru.usedesk.knowledgebase_sdk.data.repository.api.entity.*
import ru.usedesk.knowledgebase_sdk.entity.*
import toothpick.InjectConstructor
import java.util.*

@InjectConstructor
internal class KnowledgeBaseApiRepository(
        private val configuration: UsedeskKnowledgeBaseConfiguration,
        apiFactory: IUsedeskApiFactory,
        gson: Gson
) : UsedeskApiRepository<ApiRetrofit>(apiFactory, gson, ApiRetrofit::class.java), IKnowledgeBaseApiRepository {

    private var sections: List<UsedeskSection>? = null

    override fun getSections(): List<UsedeskSection> {
        return sections ?: loadSections().also {
            sections = it
        }
    }

    override fun getCategories(sectionId: Long): List<UsedeskCategory> {
        return getSections().firstOrNull {
            it.id == sectionId
        }?.categories
                ?: throw UsedeskDataNotFoundException("Categories not found by section id($sectionId)")
    }

    override fun getArticles(categoryId: Long): List<UsedeskArticleInfo> {
        return getSections().flatMap {
            it.categories
        }.firstOrNull {
            it.id == categoryId
        }?.articles
                ?: throw UsedeskDataNotFoundException("Articles not found by category id($categoryId)")
    }

    override fun getArticle(articleId: Long): UsedeskArticleContent {
        val articleContentResponse = doRequest(configuration.urlApi, ArticleContentResponse::class.java) {
            it.getArticleContent(configuration.accountId, articleId, configuration.token)
        }
        return valueOrNull {
            UsedeskArticleContent(articleContentResponse.id!!,
                    articleContentResponse.title ?: "",
                    articleContentResponse.text ?: "",
                    articleContentResponse.categoryId?.toLongOrNull()!!
            )
        } ?: throw UsedeskHttpException("Wrong response")
    }

    override fun getArticles(searchQueryRequest: SearchQueryRequest): List<UsedeskArticleContent> {
        val articlesSearchResponse = doRequest(configuration.urlApi, ArticlesSearchResponse::class.java) {
            it.getArticles(configuration.accountId,
                    configuration.token,
                    searchQueryRequest.query,
                    searchQueryRequest.count,
                    searchQueryRequest.sectionIds?.joinToString(","),
                    searchQueryRequest.categoryIds?.joinToString(","),
                    searchQueryRequest.articleIds?.joinToString(","),
                    searchQueryRequest.page,
                    searchQueryRequest.type?.name?.toLowerCase(),
                    searchQueryRequest.sort?.name?.toLowerCase(),
                    searchQueryRequest.order?.name?.toLowerCase())
        }

        return (articlesSearchResponse.articles ?: arrayOf()).mapNotNull {
            valueOrNull {
                UsedeskArticleContent(it!!.id!!,
                        it.title ?: "",
                        it.text ?: "",
                        it.categoryId?.toLongOrNull()!!)
            }
        }
    }

    override fun addViews(articleId: Long) {
        doRequest(configuration.urlApi, AddViewsResponse::class.java) {
            it.addViews(configuration.accountId, articleId, configuration.token, 1)
        }
    }

    override fun sendRating(articleId: Long,
                            good: Boolean) {
        doRequest(configuration.urlApi, ChangeRatingResponse::class.java) {
            it.changeRating(configuration.accountId,
                    articleId,
                    if (good) 1 else 0,
                    if (good) 0 else 1)
        }
    }

    override fun sendRating(articleId: Long,
                            message: String) {
        doRequest(configuration.urlApi, CreateTicketResponse::class.java) {
            it.createTicket(CreateTicketRequest(
                    configuration.token,
                    configuration.clientEmail,
                    configuration.clientName,
                    message,
                    articleId
            ))
        }
    }

    private fun loadSections(): List<UsedeskSection> {
        return doRequest(configuration.urlApi, Array<SectionResponse>::class.java) {
            it.getSections(configuration.accountId, configuration.token)
        }.mapNotNull { sectionResponse ->
            valueOrNull {
                val categories = sectionResponse.categories?.mapNotNull { categoryResponse ->
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
                                articles)
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