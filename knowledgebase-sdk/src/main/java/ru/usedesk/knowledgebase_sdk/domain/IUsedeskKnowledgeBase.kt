package ru.usedesk.knowledgebase_sdk.domain

import io.reactivex.Completable
import io.reactivex.Single
import ru.usedesk.common_sdk.entity.exceptions.UsedeskException
import ru.usedesk.knowledgebase_sdk.entity.*

interface IUsedeskKnowledgeBase {
    @Throws(UsedeskException::class)
    fun getSections(): List<UsedeskSection>

    @Throws(UsedeskException::class)
    fun getArticle(articleId: Long): UsedeskArticleContent

    @Throws(UsedeskException::class)
    fun getArticles(searchQuery: String): List<UsedeskArticleContent>

    @Throws(UsedeskException::class)
    fun getArticles(searchQuery: UsedeskSearchQuery): List<UsedeskArticleContent>

    @Throws(UsedeskException::class)
    fun getCategories(sectionId: Long): List<UsedeskCategory>

    @Throws(UsedeskException::class)
    fun getArticles(categoryId: Long): List<UsedeskArticleInfo>

    @Throws(UsedeskException::class)
    fun addViews(articleId: Long)

    fun getSectionsRx(): Single<List<UsedeskSection>>

    fun getArticleRx(articleId: Long): Single<UsedeskArticleContent>

    fun getArticlesRx(searchQuery: String): Single<List<UsedeskArticleContent>>

    fun getArticlesRx(searchQuery: UsedeskSearchQuery): Single<List<UsedeskArticleContent>>

    fun getCategoriesRx(sectionId: Long): Single<List<UsedeskCategory>>

    fun getArticlesRx(categoryId: Long): Single<List<UsedeskArticleInfo>>

    fun addViewsRx(articleId: Long): Completable
}