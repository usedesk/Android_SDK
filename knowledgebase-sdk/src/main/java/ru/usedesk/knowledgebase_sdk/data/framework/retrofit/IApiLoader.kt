package ru.usedesk.knowledgebase_sdk.data.framework.retrofit

import ru.usedesk.common_sdk.entity.exceptions.UsedeskHttpException
import ru.usedesk.knowledgebase_sdk.data.repository.entity.UsedeskArticleBodyOld
import ru.usedesk.knowledgebase_sdk.data.repository.entity.UsedeskSectionOld

internal interface IApiLoader {
    @Throws(UsedeskHttpException::class)
    fun getSections(accountId: String, token: String): Array<UsedeskSectionOld>

    @Throws(UsedeskHttpException::class)
    fun getArticle(accountId: String, articleId: String, token: String): UsedeskArticleBodyOld

    @Throws(UsedeskHttpException::class)
    fun getArticles(accountId: String, token: String, searchQuery: UsedeskSearchQueryOld): List<UsedeskArticleBodyOld>

    @Throws(UsedeskHttpException::class)
    fun addViews(accountId: String, token: String, articleId: Long, count: Int): Int
}