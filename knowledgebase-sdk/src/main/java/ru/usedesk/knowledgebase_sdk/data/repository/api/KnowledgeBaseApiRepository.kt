package ru.usedesk.knowledgebase_sdk.data.repository.api

import com.google.gson.Gson
import ru.usedesk.common_sdk.api.IUsedeskApiFactory
import ru.usedesk.common_sdk.entity.exceptions.UsedeskDataNotFoundException
import ru.usedesk.common_sdk.entity.exceptions.UsedeskHttpException
import ru.usedesk.knowledgebase_sdk.data.repository.api.entity.*
import ru.usedesk.knowledgebase_sdk.entity.*
import toothpick.InjectConstructor
import java.util.*

@InjectConstructor
internal class KnowledgeBaseApiRepository(
        apiFactory: IUsedeskApiFactory,
        gson: Gson
) : UsedeskApiRepository<ApiRetrofit>(apiFactory, gson, ApiRetrofit::class.java), IKnowledgeBaseApiRepository {

    private var sections: List<UsedeskSection>? = null

    override fun getSections(accountId: String, token: String): List<UsedeskSection> {
        return sections ?: loadSections(accountId, token).also {
            sections = it
        }
    }

    override fun getCategories(accountId: String,
                               token: String,
                               sectionId: Long): List<UsedeskCategory> {
        return getSections(accountId, token).firstOrNull {
            it.id == sectionId
        }?.categories
                ?: throw UsedeskDataNotFoundException("Categories not found by section id($sectionId)")
    }

    override fun getArticles(accountId: String,
                             token: String,
                             categoryId: Long): List<UsedeskArticleInfo> {
        return getSections(accountId, token).flatMap {
            it.categories
        }.firstOrNull {
            it.id == categoryId
        }?.articles
                ?: throw UsedeskDataNotFoundException("Articles not found by category id($categoryId)")
    }

    override fun getArticle(accountId: String,
                            token: String,
                            articleId: Long): UsedeskArticleContent {
        val articleContentResponse = doRequest(ArticleContentResponse::class.java) {
            it.getArticleContent(accountId, articleId, token)
        }
        return valueOrNull {
            UsedeskArticleContent(articleContentResponse.id!!,
                    articleContentResponse.title ?: "",
                    articleContentResponse.text ?: ""
            )
        } ?: throw UsedeskHttpException("Wrong response")
    }

    override fun getArticles(accountId: String,
                             token: String,
                             searchQuery: UsedeskSearchQuery): List<UsedeskArticleContent> {
        val articlesSearchResponse = doRequest(ArticlesSearchResponse::class.java) {
            it.getArticles(accountId,
                    token,
                    searchQuery.searchQuery,
                    searchQuery.count,
                    searchQuery.getCollectionIds(),
                    searchQuery.getCategoryIds(),
                    searchQuery.getArticleIds(),
                    searchQuery.page,
                    searchQuery.type,
                    searchQuery.sort,
                    searchQuery.order)
        }

        return (articlesSearchResponse.articles ?: arrayOf()).mapNotNull {
            valueOrNull {
                UsedeskArticleContent(it!!.id!!,
                        it.title ?: "",
                        it.text ?: "")
            }
        }
    }

    override fun addViews(accountId: String,
                          token: String,
                          articleId: Long) {
        doRequest(AddViewsResponse::class.java) {
            it.addViews(accountId, articleId, token, 1)
        }
    }

    private fun loadSections(accountId: String,
                             token: String): List<UsedeskSection> {
        return doRequest(Array<SectionResponse>::class.java) {
            it.getSections(accountId, token)
        }.mapNotNull { sectionResponse ->
            valueOrNull {
                val categories = (sectionResponse.categories ?: arrayOf())
                        .filterNotNull()
                        .map { categoryResponse ->
                            val articles = (categoryResponse.articles ?: arrayOf())
                                    .filterNotNull()
                                    .map { articleResponse ->
                                        UsedeskArticleInfo(
                                                articleResponse.id!!,
                                                articleResponse.title ?: "",
                                                articleResponse.views ?: 0
                                        )
                                    }
                            UsedeskCategory(
                                    categoryResponse.id!!,
                                    categoryResponse.title ?: "",
                                    categoryResponse.description ?: "",
                                    articles,
                                    categoryResponse.order ?: 0)
                        }
                UsedeskSection(
                        sectionResponse.id!!,
                        sectionResponse.title ?: "",
                        null,//sectionResponse.image,
                        categories,
                        sectionResponse.order ?: 0
                )
            }
        }
    }
}