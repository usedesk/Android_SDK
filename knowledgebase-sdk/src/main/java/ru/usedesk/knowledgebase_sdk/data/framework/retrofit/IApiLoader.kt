package ru.usedesk.knowledgebase_sdk.data.framework.retrofit

import ru.usedesk.common_sdk.entity.exceptions.UsedeskHttpException
import ru.usedesk.knowledgebase_sdk.entity.UsedeskArticleBody
import ru.usedesk.knowledgebase_sdk.entity.UsedeskSearchQuery
import ru.usedesk.knowledgebase_sdk.entity.UsedeskSection

internal interface IApiLoader {
    @Throws(UsedeskHttpException::class)
    fun getSections(accountId: String, token: String): Array<UsedeskSection>

    @Throws(UsedeskHttpException::class)
    fun getArticle(accountId: String, articleId: String, token: String): UsedeskArticleBody

    @Throws(UsedeskHttpException::class)
    fun getArticles(accountId: String, token: String, searchQuery: UsedeskSearchQuery): List<UsedeskArticleBody>

    @Throws(UsedeskHttpException::class)
    fun addViews(accountId: String, token: String, articleId: Long, count: Int): Int
}