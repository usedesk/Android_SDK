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
        private val mainThreadScheduler: Scheduler
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

    override fun sendRatingRx(articleId: Long, good: Boolean): Completable {
        return Completable.create { emitter: CompletableEmitter ->
            sendRating(articleId, good)
            emitter.onComplete()
        }.subscribeOn(ioScheduler)
                .observeOn(mainThreadScheduler)
    }

    override fun sendRatingRx(articleId: Long, message: String): Completable {
        return Completable.create { emitter: CompletableEmitter ->
            sendRating(articleId, message)
            emitter.onComplete()
        }.subscribeOn(ioScheduler)
                .observeOn(mainThreadScheduler)
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
        val query = UsedeskSearchQuery.Builder(searchQuery).build()
        return knowledgeApiRepository.getArticles(query)
    }

    @Throws(UsedeskException::class)
    override fun getArticles(searchQuery: UsedeskSearchQuery): List<UsedeskArticleContent> {
        return knowledgeApiRepository.getArticles(searchQuery)
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