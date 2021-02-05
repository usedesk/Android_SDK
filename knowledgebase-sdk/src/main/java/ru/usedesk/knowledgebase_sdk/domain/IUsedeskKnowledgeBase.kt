package ru.usedesk.knowledgebase_sdk.domain

import io.reactivex.Completable
import io.reactivex.Single
import ru.usedesk.common_sdk.entity.exceptions.UsedeskException
import ru.usedesk.knowledgebase_sdk.entity.UsedeskArticleContent
import ru.usedesk.knowledgebase_sdk.entity.UsedeskArticleInfo
import ru.usedesk.knowledgebase_sdk.entity.UsedeskCategory
import ru.usedesk.knowledgebase_sdk.entity.UsedeskSection

interface IUsedeskKnowledgeBase {
    @Throws(UsedeskException::class)
    fun getSections(): List<UsedeskSection>

    fun getSectionsRx(): Single<List<UsedeskSection>>

    @Throws(UsedeskException::class)
    fun getCategories(sectionId: Long): List<UsedeskCategory>

    fun getCategoriesRx(sectionId: Long): Single<List<UsedeskCategory>>

    @Throws(UsedeskException::class)
    fun getArticles(categoryId: Long): List<UsedeskArticleInfo>

    fun getArticlesRx(categoryId: Long): Single<List<UsedeskArticleInfo>>

    @Throws(UsedeskException::class)
    fun getArticles(searchQuery: String): List<UsedeskArticleContent>

    fun getArticlesRx(searchQuery: String): Single<List<UsedeskArticleContent>>

    @Throws(UsedeskException::class)
    fun getArticle(articleId: Long): UsedeskArticleContent

    fun getArticleRx(articleId: Long): Single<UsedeskArticleContent>

    @Throws(UsedeskException::class)
    fun addViews(articleId: Long)

    fun addViewsRx(articleId: Long): Completable

    @Throws(UsedeskException::class)
    fun sendRating(articleId: Long, good: Boolean)

    fun sendRatingRx(articleId: Long, good: Boolean): Completable

    @Throws(UsedeskException::class)
    fun sendRating(articleId: Long, message: String)

    fun sendRatingRx(articleId: Long, message: String): Completable
}