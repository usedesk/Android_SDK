package ru.usedesk.knowledgebase_sdk.data.repository.api

import ru.usedesk.knowledgebase_sdk.data.repository.api.entity.SearchQueryRequest
import ru.usedesk.knowledgebase_sdk.entity.UsedeskArticleContent
import ru.usedesk.knowledgebase_sdk.entity.UsedeskArticleInfo
import ru.usedesk.knowledgebase_sdk.entity.UsedeskCategory
import ru.usedesk.knowledgebase_sdk.entity.UsedeskSection

internal interface IKnowledgeBaseApi {
    fun getSections(): List<UsedeskSection>

    fun getCategories(sectionId: Long): List<UsedeskCategory>

    fun getArticles(categoryId: Long): List<UsedeskArticleInfo>

    fun getArticle(articleId: Long): UsedeskArticleContent

    fun getArticles(searchQueryRequest: SearchQueryRequest): List<UsedeskArticleContent>

    fun addViews(articleId: Long)

    fun sendRating(
        articleId: Long,
        good: Boolean
    )

    fun sendRating(
        articleId: Long,
        message: String
    )
}