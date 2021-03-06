package ru.usedesk.knowledgebase_sdk.domain

import io.reactivex.Completable
import io.reactivex.Single
import ru.usedesk.common_sdk.entity.exceptions.UsedeskException
import ru.usedesk.common_sdk.utils.UsedeskRxUtil.safeCompletableIo
import ru.usedesk.common_sdk.utils.UsedeskRxUtil.safeSingleIo
import ru.usedesk.knowledgebase_sdk.data.repository.api.IKnowledgeBaseApiRepository
import ru.usedesk.knowledgebase_sdk.data.repository.api.entity.SearchQueryRequest
import ru.usedesk.knowledgebase_sdk.entity.UsedeskArticleContent
import ru.usedesk.knowledgebase_sdk.entity.UsedeskArticleInfo
import ru.usedesk.knowledgebase_sdk.entity.UsedeskCategory
import ru.usedesk.knowledgebase_sdk.entity.UsedeskSection
import toothpick.InjectConstructor

@InjectConstructor
internal class KnowledgeBaseInteractor(
        private val knowledgeApiRepository: IKnowledgeBaseApiRepository
) : IUsedeskKnowledgeBase {

    override fun getSectionsRx(): Single<List<UsedeskSection>> {
        return safeSingleIo {
            getSections()
        }
    }

    override fun getArticleRx(articleId: Long): Single<UsedeskArticleContent> {
        return safeSingleIo {
            getArticle(articleId)
        }
    }

    override fun getCategoriesRx(sectionId: Long): Single<List<UsedeskCategory>> {
        return safeSingleIo {
            getCategories(sectionId)
        }
    }

    override fun getArticlesRx(categoryId: Long): Single<List<UsedeskArticleInfo>> {
        return safeSingleIo {
            getArticles(categoryId)
        }
    }

    override fun getArticlesRx(searchQuery: String): Single<List<UsedeskArticleContent>> {
        return safeSingleIo {
            getArticles(searchQuery)
        }
    }

    override fun addViewsRx(articleId: Long): Completable {
        return safeCompletableIo {
            addViews(articleId)
        }
    }

    override fun sendRatingRx(articleId: Long, good: Boolean): Completable {
        return safeCompletableIo {
            sendRating(articleId, good)
        }
    }

    override fun sendRatingRx(articleId: Long, message: String): Completable {
        return safeCompletableIo {
            sendRating(articleId, message)
        }
    }

    @Throws(UsedeskException::class)
    override fun getCategories(sectionId: Long): List<UsedeskCategory> {
        return knowledgeApiRepository.getCategories(sectionId)
    }

    @Throws(UsedeskException::class)
    override fun getSections(): List<UsedeskSection> {
        return knowledgeApiRepository.getSections()
    }

    @Throws(UsedeskException::class)
    override fun getArticle(articleId: Long): UsedeskArticleContent {
        return knowledgeApiRepository.getArticle(articleId)
    }

    @Throws(UsedeskException::class)
    override fun getArticles(searchQuery: String): List<UsedeskArticleContent> {
        val query = SearchQueryRequest(searchQuery)
        return knowledgeApiRepository.getArticles(query)
    }

    @Throws(UsedeskException::class)
    override fun getArticles(categoryId: Long): List<UsedeskArticleInfo> {
        return knowledgeApiRepository.getArticles(categoryId)
    }

    @Throws(UsedeskException::class)
    override fun addViews(articleId: Long) {
        knowledgeApiRepository.addViews(articleId)
    }

    override fun sendRating(articleId: Long, good: Boolean) {
        knowledgeApiRepository.sendRating(articleId,
                good)
    }

    override fun sendRating(articleId: Long, message: String) {
        knowledgeApiRepository.sendRating(articleId,
                message)
    }
}