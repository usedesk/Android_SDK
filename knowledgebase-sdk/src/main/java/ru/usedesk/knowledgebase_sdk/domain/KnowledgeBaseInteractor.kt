package ru.usedesk.knowledgebase_sdk.domain

import io.reactivex.*
import ru.usedesk.common_sdk.entity.exceptions.UsedeskException
import ru.usedesk.knowledgebase_sdk.data.repository.api.IKnowledgeBaseApiRepository
import ru.usedesk.knowledgebase_sdk.entity.*
import toothpick.InjectConstructor
import javax.inject.Named

@InjectConstructor
internal class KnowledgeBaseInteractor(
        private val knowledgeApiRepository: IKnowledgeBaseApiRepository,
        @Named("io")
        private val ioScheduler: Scheduler,
        @Named("main")
        private val mainThreadScheduler: Scheduler,
        private val configuration: UsedeskKnowledgeBaseConfiguration
) : IUsedeskKnowledgeBase {

    private fun <T> createSingle(emitter: SingleOnSubscribe<T>): Single<T> {
        return Single.create(SafeSingleEmitter(emitter))
                .subscribeOn(ioScheduler)
                .observeOn(mainThreadScheduler)
    }

    override fun getSectionsRx(): Single<List<UsedeskSection>> {
        return createSingle {
            it.onSuccess(getSections())
        }
    }

    override fun getArticleRx(articleId: Long): Single<UsedeskArticleContent> {
        return createSingle {
            it.onSuccess(getArticle(articleId))
        }
    }

    override fun getCategoriesRx(sectionId: Long): Single<List<UsedeskCategory>> {
        return createSingle {
            it.onSuccess(getCategories(sectionId))
        }
    }

    override fun getArticlesRx(categoryId: Long): Single<List<UsedeskArticleInfo>> {
        return createSingle {
            it.onSuccess(getArticles(categoryId))
        }
    }

    override fun getArticlesRx(searchQuery: String): Single<List<UsedeskArticleContent>> {
        return createSingle {
            it.onSuccess(getArticles(searchQuery))
        }
    }

    override fun getArticlesRx(searchQuery: UsedeskSearchQuery): Single<List<UsedeskArticleContent>> {
        return createSingle {
            it.onSuccess(getArticles(searchQuery))
        }
    }

    override fun addViewsRx(articleId: Long): Completable {
        return Completable.create { emitter: CompletableEmitter ->
            addViews(articleId)
            emitter.onComplete()
        }.subscribeOn(ioScheduler)
                .observeOn(mainThreadScheduler)
    }

    override fun sendFeedbackRx(articleId: Long, good: Boolean): Completable {
        return Completable.create { emitter: CompletableEmitter ->
            sendFeedback(articleId, good)
            emitter.onComplete()
        }.subscribeOn(ioScheduler)
                .observeOn(mainThreadScheduler)
    }

    override fun sendFeedbackRx(articleId: Long, message: String): Completable {
        return Completable.create { emitter: CompletableEmitter ->
            sendFeedback(articleId, message)
            emitter.onComplete()
        }.subscribeOn(ioScheduler)
                .observeOn(mainThreadScheduler)
    }

    @Throws(UsedeskException::class)
    override fun getCategories(sectionId: Long): List<UsedeskCategory> {
        return knowledgeApiRepository.getCategories(configuration.accountId,
                configuration.token,
                sectionId)
    }

    @Throws(UsedeskException::class)
    override fun getSections(): List<UsedeskSection> {
        return knowledgeApiRepository.getSections(configuration.accountId,
                configuration.token)
    }

    @Throws(UsedeskException::class)
    override fun getArticle(articleId: Long): UsedeskArticleContent {
        return knowledgeApiRepository.getArticle(configuration.accountId,
                configuration.token,
                articleId)
    }

    @Throws(UsedeskException::class)
    override fun getArticles(searchQuery: String): List<UsedeskArticleContent> {
        val query = UsedeskSearchQuery.Builder(searchQuery).build()
        return knowledgeApiRepository.getArticles(configuration.accountId,
                configuration.token,
                query)
    }

    @Throws(UsedeskException::class)
    override fun getArticles(searchQuery: UsedeskSearchQuery): List<UsedeskArticleContent> {
        return knowledgeApiRepository.getArticles(configuration.accountId,
                configuration.token,
                searchQuery)
    }

    @Throws(UsedeskException::class)
    override fun getArticles(categoryId: Long): List<UsedeskArticleInfo> {
        return knowledgeApiRepository.getArticles(configuration.accountId,
                configuration.token,
                categoryId)
    }

    @Throws(UsedeskException::class)
    override fun addViews(articleId: Long) {
        knowledgeApiRepository.addViews(configuration.accountId,
                configuration.token,
                articleId)
    }

    override fun sendFeedback(articleId: Long, good: Boolean) {
        knowledgeApiRepository.sendFeedback(configuration.accountId,
                configuration.token,
                articleId,
                good)
    }

    override fun sendFeedback(articleId: Long, message: String) {
        knowledgeApiRepository.sendFeedback(configuration.accountId,
                configuration.token,
                articleId,
                message)
    }

    internal class SafeSingleEmitter<T>(
            private val singleOnSubscribeSafe: SingleOnSubscribe<T>
    ) : SingleOnSubscribe<T> {

        @Throws(Exception::class)
        override fun subscribe(emitter: SingleEmitter<T>) {
            try {
                singleOnSubscribeSafe.subscribe(emitter)
            } catch (e: Exception) {
                if (!emitter.isDisposed) {
                    throw e
                }
            }
        }
    }
}