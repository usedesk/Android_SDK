package ru.usedesk.knowledgebase_sdk.data.repository.api

import ru.usedesk.knowledgebase_sdk.entity.*

internal interface IKnowledgeBaseApiRepository {
    fun getSections(): List<UsedeskSection>

    fun getCategories(sectionId: Long): List<UsedeskCategory>

    fun getArticles(categoryId: Long): List<UsedeskArticleInfo>

    fun getArticle(articleId: Long): UsedeskArticleContent

    fun getArticles(searchQuery: UsedeskSearchQuery): List<UsedeskArticleContent>

    fun addViews(articleId: Long)

    fun sendRating(articleId: Long,
                   good: Boolean)

    fun sendRating(articleId: Long,
                   message: String)
}