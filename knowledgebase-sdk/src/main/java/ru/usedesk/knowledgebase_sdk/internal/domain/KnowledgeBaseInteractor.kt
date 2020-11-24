package ru.usedesk.knowledgebase_sdk.internal.domain

import io.reactivex.*
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskException
import ru.usedesk.knowledgebase_sdk.external.IUsedeskKnowledgeBase
import ru.usedesk.knowledgebase_sdk.external.entity.*
import ru.usedesk.knowledgebase_sdk.internal.data.repository.IKnowledgeBaseRepository
import toothpick.InjectConstructor
import javax.inject.Named

@InjectConstructor
class KnowledgeBaseInteractor(
        private val knowledgeRepository: IKnowledgeBaseRepository,
        @Named("work")
        private val workScheduler: Scheduler,
        @Named("main")
        private val mainThreadScheduler: Scheduler,
        private val configuration: UsedeskKnowledgeBaseConfiguration
) : IUsedeskKnowledgeBase {

    private fun <T> createSingle(emitter: SingleOnSubscribe<T>): Single<T> {
        return Single.create(SafeSingleEmitter(emitter))
                .subscribeOn(workScheduler)
                .observeOn(mainThreadScheduler)
    }

    override fun getSectionsRx(): Single<List<UsedeskSection>> {
        return createSingle {
            it.onSuccess(getSections())
        }
    }

    override fun getArticleRx(articleId: Long): Single<UsedeskArticleBody> {
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

    override fun getArticlesRx(searchQuery: String): Single<List<UsedeskArticleBody>> {
        return createSingle {
            it.onSuccess(getArticles(searchQuery))
        }
    }

    override fun getArticlesRx(searchQuery: UsedeskSearchQuery): Single<List<UsedeskArticleBody>> {
        return createSingle {
            it.onSuccess(getArticles(searchQuery))
        }
    }

    override fun addViewsRx(articleId: Long): Completable {
        return Completable.create { emitter: CompletableEmitter ->
            addViews(articleId)
            emitter.onComplete()
        }.subscribeOn(workScheduler)
                .observeOn(mainThreadScheduler)
    }

    @Throws(UsedeskException::class)
    override fun addViews(articleId: Long) {
        knowledgeRepository.addViews(configuration.accountId, configuration.token, articleId)
    }

    @Throws(UsedeskException::class)
    override fun getCategories(sectionId: Long): List<UsedeskCategory> {
        return knowledgeRepository.getCategories(configuration.accountId, configuration.token, sectionId)
    }

    @Throws(UsedeskException::class)
    override fun getSections(): List<UsedeskSection> {
        return knowledgeRepository.getSections(configuration.accountId, configuration.token)
    }

    @Throws(UsedeskException::class)
    override fun getArticle(articleId: Long): UsedeskArticleBody {
        return knowledgeRepository.getArticleBody(configuration.accountId, configuration.token, articleId)
    }

    @Throws(UsedeskException::class)
    override fun getArticles(searchQuery: String): List<UsedeskArticleBody> {
        val query = UsedeskSearchQuery.Builder(searchQuery).build()
        return knowledgeRepository.getArticles(configuration.accountId, configuration.token, query)
    }

    @Throws(UsedeskException::class)
    override fun getArticles(searchQuery: UsedeskSearchQuery): List<UsedeskArticleBody> {
        return knowledgeRepository.getArticles(configuration.accountId, configuration.token, searchQuery)
    }

    @Throws(UsedeskException::class)
    override fun getArticles(categoryId: Long): List<UsedeskArticleInfo> {
        return knowledgeRepository.getArticles(configuration.accountId, configuration.token, categoryId)
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