package ru.usedesk.knowledgebase_sdk.data.repository

import com.google.gson.Gson
import ru.usedesk.common_sdk.api.IUsedeskApiFactory
import ru.usedesk.common_sdk.entity.exceptions.UsedeskDataNotFoundException
import ru.usedesk.common_sdk.entity.exceptions.UsedeskHttpException
import ru.usedesk.knowledgebase_sdk.data.framework.retrofit.UsedeskApiRepository
import ru.usedesk.knowledgebase_sdk.data.framework.retrofit.entity.ArticlesBodyPage
import ru.usedesk.knowledgebase_sdk.data.framework.retrofit.entity.ViewsAdded
import ru.usedesk.knowledgebase_sdk.data.repository.entity.UsedeskArticleBodyResponse
import ru.usedesk.knowledgebase_sdk.data.repository.entity.UsedeskSectionResponse
import ru.usedesk.knowledgebase_sdk.entity.*
import toothpick.InjectConstructor

@InjectConstructor
internal class ApiRepository(
        apiFactory: IUsedeskApiFactory,
        gson: Gson
) : UsedeskApiRepository(apiFactory, gson), IKnowledgeBaseRepository {

    private var sectionList: List<UsedeskSection>? = null

    @Throws(UsedeskHttpException::class)
    override fun getSections(accountId: String, token: String): List<UsedeskSection> {
        return sectionList ?: loadSections(accountId, token).also {
            sectionList = it
        }
    }

    @Throws(UsedeskHttpException::class)
    override fun getArticleBody(accountId: String,
                                token: String,
                                articleId: Long): UsedeskArticleBody {
        return loadArticleBody(accountId, articleId, token)
    }

    @Throws(UsedeskHttpException::class)
    override fun getArticles(accountId: String,
                             token: String,
                             searchQuery: UsedeskSearchQuery): List<UsedeskArticleBody> {
        return apiLoader.getArticles(accountId, token, searchQuery)
    }

    @Throws(UsedeskHttpException::class, UsedeskDataNotFoundException::class)
    override fun getCategories(accountId: String,
                               token: String,
                               sectionId: Long): List<UsedeskCategory> {
        if (sectionList == null) {
            getSections(accountId, token)
        }
        return getCategories(sectionId).toList()
    }

    @Throws(UsedeskHttpException::class, UsedeskDataNotFoundException::class)
    override fun getArticles(accountId: String,
                             token: String,
                             categoryId: Long): List<UsedeskArticleInfo> {
        if (sectionList == null) {
            getSections(accountId, token)
        }
        return getArticles(categoryId).toList()
    }

    @Throws(UsedeskHttpException::class, UsedeskDataNotFoundException::class)
    override fun addViews(accountId: String,
                          token: String,
                          articleId: Long) {
        val views = apiLoader.addViews(accountId, token, articleId, 1)
    }

    @Throws(UsedeskDataNotFoundException::class)
    private fun getArticleInfo(articleId: Long): UsedeskArticleInfo {
        return sectionList?.asSequence()?.mapNotNull { section ->
            section.categories
        }?.flatMap { categories ->
            categories.asSequence()
        }?.mapNotNull { category ->
            category.articles
        }?.flatMap { articles ->
            articles.asSequence()
        }?.first { article ->
            article.id == articleId
        } ?: throw UsedeskDataNotFoundException("UsedeskArticleInfo with id($articleId)")
    }

    @Throws(UsedeskDataNotFoundException::class)
    private fun getArticles(categoryId: Long): List<UsedeskArticleInfo> {
        return sectionList?.map { section ->
            section.categories
        }?.flatMap { categories ->
            categories.asSequence()
        }?.firstOrNull { category ->
            category.id == categoryId
        }?.articles ?: throw UsedeskDataNotFoundException("UsedeskCategory with id($categoryId)")
    }

    private fun loadSections(accountId: String,
                             token: String): Array<UsedeskSectionResponse> {
        val sectionsResponse = executeRequest(Array<UsedeskSectionResponse>::class.java,
                apiRetrofit.getSections(accountId, token))
    }

    private fun loadArticle(accountId: String, articleId: String,
                            token: String): UsedeskArticleBodyResponse {
        return executeRequest(UsedeskArticleBodyResponse::class.java, apiRetrofit.getArticleBody(accountId, articleId, token))
    }

    private fun getArticles(accountId: String, token: String,
                            searchQuery: UsedeskSearchQuery): List<UsedeskArticleBodyResponse> {
        return executeRequest(ArticlesBodyPage::class.java,
                apiRetrofit.getArticlesBody(accountId,
                        token,
                        searchQuery.searchQuery,
                        searchQuery.count,
                        searchQuery.getCollectionIds(),
                        searchQuery.getCategoryIds(),
                        searchQuery.getArticleIds(),
                        searchQuery.page,
                        searchQuery.type,
                        searchQuery.sort,
                        searchQuery.order))
                .articles?.toList()
                ?: listOf()
    }

    private fun addViews(accountId: String, token: String, articleId: Long, count: Int): Int {
        return executeRequest(ViewsAdded::class.java,
                apiRetrofit.addViews(accountId, articleId, token, count))
                .views
    }

    private fun loadArticleBody(accountId: String,
                                token: String,
                                articleId: Long): UsedeskArticleBody {
        return apiLoader.getArticle(accountId, articleId, token)
    }

    private fun getCategories(sectionId: Long): List<UsedeskCategory> {
        return sectionList?.first { section ->
            section.id == sectionId
        }?.categories ?: throw UsedeskDataNotFoundException("UsedeskSection with id($sectionId)")
    }
}