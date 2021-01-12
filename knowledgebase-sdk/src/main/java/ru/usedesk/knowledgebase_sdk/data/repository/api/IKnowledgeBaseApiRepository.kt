package ru.usedesk.knowledgebase_sdk.data.repository.api

import ru.usedesk.knowledgebase_sdk.entity.*

internal interface IKnowledgeBaseApiRepository {
    fun getSections(accountId: String, token: String): List<UsedeskSection>

    fun getCategories(accountId: String, token: String, sectionId: Long): List<UsedeskCategory>

    fun getArticles(accountId: String, token: String, categoryId: Long): List<UsedeskArticleInfo>

    fun getArticle(accountId: String, token: String, articleId: Long): UsedeskArticleContent

    fun getArticles(accountId: String, token: String, searchQuery: UsedeskSearchQuery): List<UsedeskArticleContent>

    fun addViews(accountId: String, token: String, articleId: Long)

    fun sendFeedback(accountId: String, token: String, articleId: Long, good: Boolean)

    fun sendFeedback(accountId: String, token: String, articleId: Long, message: String)
}