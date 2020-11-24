package ru.usedesk.knowledgebase_sdk.internal.data.repository

import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskDataNotFoundException
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskHttpException
import ru.usedesk.knowledgebase_sdk.external.entity.*
import ru.usedesk.knowledgebase_sdk.internal.data.framework.retrofit.IApiLoader
import toothpick.InjectConstructor

@InjectConstructor
class ApiRepository(
        private val apiLoader: IApiLoader
) : IKnowledgeBaseRepository {

    private var sectionList: List<UsedeskSection>? = null

    @Throws(UsedeskHttpException::class)
    override fun getSections(accountId: String, token: String): List<UsedeskSection> {
        if (sectionList == null) {
            sectionList = apiLoader.getSections(accountId, token).toList()
        }
        return sectionList!!
    }

    @Throws(UsedeskHttpException::class)
    override fun getArticleBody(accountId: String, token: String, articleId: Long): UsedeskArticleBody {
        return apiLoader.getArticle(accountId, articleId.toString(), token)
    }

    @Throws(UsedeskHttpException::class)
    override fun getArticles(accountId: String,
                             token: String,
                             searchQuery: UsedeskSearchQuery): List<UsedeskArticleBody> {
        return apiLoader.getArticles(accountId, token, searchQuery)
    }

    @Throws(UsedeskHttpException::class, UsedeskDataNotFoundException::class)
    override fun getCategories(accountId: String, token: String, sectionId: Long): List<UsedeskCategory> {
        if (sectionList == null) {
            getSections(accountId, token)
        }
        return getCategories(sectionId).toList()
    }

    @Throws(UsedeskHttpException::class, UsedeskDataNotFoundException::class)
    override fun getArticles(accountId: String, token: String, categoryId: Long): List<UsedeskArticleInfo> {
        if (sectionList == null) {
            getSections(accountId, token)
        }
        return getArticles(categoryId).toList()
    }

    @Throws(UsedeskHttpException::class, UsedeskDataNotFoundException::class)
    override fun addViews(accountId: String, token: String, articleId: Long) {
        val views = apiLoader.addViews(accountId, token, articleId, 1)
        getArticleBody(accountId, token, articleId).views = views
        getArticleInfo(articleId).views = views
    }

    @Throws(UsedeskDataNotFoundException::class)
    private fun getArticleInfo(articleId: Long): UsedeskArticleInfo {
        for (section in sectionList!!) {
            for (category in section.categories) for (articleInfo in category.articles) {
                if (articleInfo.id == articleId) {
                    return articleInfo
                }
            }
        }
        throw UsedeskDataNotFoundException()
    }

    @Throws(UsedeskDataNotFoundException::class)
    private fun getArticles(categoryId: Long): Array<UsedeskArticleInfo> {
        for (section in sectionList!!) {
            for (category in section.categories) if (category.id == categoryId) {
                return category.articles
            }
        }
        throw UsedeskDataNotFoundException("UsedeskCategory with id($categoryId)")
    }

    @Throws(UsedeskDataNotFoundException::class)
    private fun getCategories(sectionId: Long): Array<UsedeskCategory> {
        for (section in sectionList!!) {
            if (section.id == sectionId) {
                return section.categories
            }
        }
        throw UsedeskDataNotFoundException("UsedeskSection with id($sectionId)")
    }
}