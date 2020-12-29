package ru.usedesk.knowledgebase_sdk.data.repository

import ru.usedesk.common_sdk.entity.exceptions.UsedeskDataNotFoundException
import ru.usedesk.common_sdk.entity.exceptions.UsedeskHttpException
import ru.usedesk.knowledgebase_sdk.entity.*

internal interface IKnowledgeBaseRepository {
    @Throws(UsedeskHttpException::class)
    fun getSections(accountId: String, token: String): List<UsedeskSection>

    @Throws(UsedeskHttpException::class)
    fun getArticleBody(accountId: String, token: String, articleId: Long): UsedeskArticleBody

    @Throws(UsedeskHttpException::class)
    fun getArticles(accountId: String, token: String, searchQuery: UsedeskSearchQuery): List<UsedeskArticleBody>

    @Throws(UsedeskHttpException::class, UsedeskDataNotFoundException::class)
    fun getCategories(accountId: String, token: String, sectionId: Long): List<UsedeskCategory>

    @Throws(UsedeskHttpException::class, UsedeskDataNotFoundException::class)
    fun getArticles(accountId: String, token: String, categoryId: Long): List<UsedeskArticleInfo>

    @Throws(UsedeskHttpException::class, UsedeskDataNotFoundException::class)
    fun addViews(accountId: String, token: String, articleId: Long)
}