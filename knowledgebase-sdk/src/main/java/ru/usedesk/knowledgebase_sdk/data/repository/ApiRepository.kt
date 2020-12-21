package ru.usedesk.knowledgebase_sdk.data.repository

import ru.usedesk.common_sdk.entity.exceptions.UsedeskDataNotFoundException
import ru.usedesk.common_sdk.entity.exceptions.UsedeskHttpException
import ru.usedesk.knowledgebase_sdk.data.framework.retrofit.IApiLoader
import ru.usedesk.knowledgebase_sdk.data.repository.entity.UsedeskArticleBodyOld
import ru.usedesk.knowledgebase_sdk.data.repository.entity.UsedeskArticleInfoOld
import ru.usedesk.knowledgebase_sdk.data.repository.entity.UsedeskCategoryOld
import ru.usedesk.knowledgebase_sdk.data.repository.entity.UsedeskSectionOld
import ru.usedesk.knowledgebase_sdk.entity.*
import toothpick.InjectConstructor

@InjectConstructor
internal class ApiRepository(
        private val apiLoader: IApiLoader
) : IKnowledgeBaseRepository {

    private var sectionList: List<UsedeskSectionOld>? = null

    @Throws(UsedeskHttpException::class)
    override fun getSections(accountId: String, token: String): List<UsedeskSectionOld> {
        if (sectionList == null) {
            sectionList = apiLoader.getSections(accountId, token).toList()
        }
        return sectionList!!
    }

    @Throws(UsedeskHttpException::class)
    override fun getArticleBody(accountId: String, token: String, articleId: Long): UsedeskArticleBodyOld {
        return apiLoader.getArticle(accountId, articleId.toString(), token)
    }

    @Throws(UsedeskHttpException::class)
    override fun getArticles(accountId: String,
                             token: String,
                             searchQuery: UsedeskSearchQueryOld): List<UsedeskArticleBodyOld> {
        return apiLoader.getArticles(accountId, token, searchQuery)
    }

    @Throws(UsedeskHttpException::class, UsedeskDataNotFoundException::class)
    override fun getCategories(accountId: String, token: String, sectionId: Long): List<UsedeskCategoryOld> {
        if (sectionList == null) {
            getSections(accountId, token)
        }
        return getCategories(sectionId).toList()
    }

    @Throws(UsedeskHttpException::class, UsedeskDataNotFoundException::class)
    override fun getArticles(accountId: String, token: String, categoryId: Long): List<UsedeskArticleInfoOld> {
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
    private fun getArticleInfo(articleId: Long): UsedeskArticleInfoOld {
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
    private fun getArticles(categoryId: Long): Array<UsedeskArticleInfoOld> {
        return sectionList?.mapNotNull { section ->
            section.categories
        }?.flatMap { categories ->
            categories.asSequence()
        }?.firstOrNull { category ->
            category.id == categoryId
        }?.articles ?: throw UsedeskDataNotFoundException("UsedeskCategory with id($categoryId)")
    }

    @Throws(UsedeskDataNotFoundException::class)
    private fun getCategories(sectionId: Long): Array<UsedeskCategoryOld> {
        return sectionList?.first { section ->
            section.id == sectionId
        }?.categories ?: throw UsedeskDataNotFoundException("UsedeskSection with id($sectionId)")
    }
}