package ru.usedesk.knowledgebase_sdk.data.repository

import ru.usedesk.common_sdk.entity.exceptions.UsedeskDataNotFoundException
import ru.usedesk.common_sdk.entity.exceptions.UsedeskHttpException
import ru.usedesk.knowledgebase_sdk.data.repository.entity.UsedeskArticleBodyOld
import ru.usedesk.knowledgebase_sdk.data.repository.entity.UsedeskArticleInfoOld
import ru.usedesk.knowledgebase_sdk.data.repository.entity.UsedeskCategoryOld
import ru.usedesk.knowledgebase_sdk.data.repository.entity.UsedeskSectionOld
import ru.usedesk.knowledgebase_sdk.entity.*

internal interface IKnowledgeBaseRepository {
    @Throws(UsedeskHttpException::class)
    fun getSections(accountId: String, token: String): List<UsedeskSectionOld>

    @Throws(UsedeskHttpException::class)
    fun getArticleBody(accountId: String, token: String, articleId: Long): UsedeskArticleBodyOld

    @Throws(UsedeskHttpException::class)
    fun getArticles(accountId: String, token: String, searchQuery: UsedeskSearchQueryOld): List<UsedeskArticleBodyOld>

    @Throws(UsedeskHttpException::class, UsedeskDataNotFoundException::class)
    fun getCategories(accountId: String, token: String, sectionId: Long): List<UsedeskCategoryOld>

    @Throws(UsedeskHttpException::class, UsedeskDataNotFoundException::class)
    fun getArticles(accountId: String, token: String, categoryId: Long): List<UsedeskArticleInfoOld>

    @Throws(UsedeskHttpException::class, UsedeskDataNotFoundException::class)
    fun addViews(accountId: String, token: String, articleId: Long)
}