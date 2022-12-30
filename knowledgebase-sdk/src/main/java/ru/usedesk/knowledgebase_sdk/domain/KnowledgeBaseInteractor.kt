package ru.usedesk.knowledgebase_sdk.domain

import io.reactivex.schedulers.Schedulers
import ru.usedesk.knowledgebase_sdk.RxUtil.safeCompletableIo
import ru.usedesk.knowledgebase_sdk.RxUtil.safeSingleIo
import ru.usedesk.knowledgebase_sdk.data.repository.api.IKnowledgeBaseApi
import ru.usedesk.knowledgebase_sdk.data.repository.api.entity.SearchQueryRequest
import ru.usedesk.knowledgebase_sdk.entity.UsedeskArticleContent
import javax.inject.Inject

internal class KnowledgeBaseInteractor @Inject constructor(
    private val knowledgeApiRepository: IKnowledgeBaseApi
) : IUsedeskKnowledgeBase {

    private val ioScheduler = Schedulers.io()

    override fun getSectionsRx() = safeSingleIo(ioScheduler, this::getSections)

    override fun getArticleRx(articleId: Long) = safeSingleIo(ioScheduler) { getArticle(articleId) }

    override fun getCategoriesRx(sectionId: Long) =
        safeSingleIo(ioScheduler) { getCategories(sectionId) }

    override fun getArticlesRx(categoryId: Long) =
        safeSingleIo(ioScheduler) { getArticles(categoryId) }

    override fun getArticlesRx(searchQuery: String) =
        safeSingleIo(ioScheduler) { getArticles(searchQuery) }

    override fun addViewsRx(articleId: Long) =
        safeCompletableIo(ioScheduler) { addViews(articleId) }

    override fun sendRatingRx(articleId: Long, good: Boolean) =
        safeCompletableIo(ioScheduler) { sendRating(articleId, good) }

    override fun sendRatingRx(articleId: Long, message: String) =
        safeCompletableIo(ioScheduler) { sendRating(articleId, message) }

    override fun getCategories(sectionId: Long) = knowledgeApiRepository.getCategories(sectionId)

    override fun getSections() = knowledgeApiRepository.getSections()

    override fun getArticle(articleId: Long) = knowledgeApiRepository.getArticle(articleId)

    override fun getArticles(searchQuery: String): List<UsedeskArticleContent> {
        val query = SearchQueryRequest(searchQuery)
        return knowledgeApiRepository.getArticles(query)
    }

    override fun getArticles(categoryId: Long) = knowledgeApiRepository.getArticles(categoryId)

    override fun addViews(articleId: Long) {
        knowledgeApiRepository.addViews(articleId)
    }

    override fun sendRating(articleId: Long, good: Boolean) {
        knowledgeApiRepository.sendRating(
            articleId,
            good
        )
    }

    override fun sendRating(articleId: Long, message: String) {
        knowledgeApiRepository.sendRating(
            articleId,
            message
        )
    }
}