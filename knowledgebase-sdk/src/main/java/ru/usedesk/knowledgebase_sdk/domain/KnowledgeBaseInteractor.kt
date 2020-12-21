package ru.usedesk.knowledgebase_sdk.domain

import io.reactivex.*
import ru.usedesk.common_sdk.entity.exceptions.UsedeskException
import ru.usedesk.knowledgebase_sdk.data.repository.IKnowledgeBaseRepository
import ru.usedesk.knowledgebase_sdk.data.repository.entity.UsedeskArticleBodyOld
import ru.usedesk.knowledgebase_sdk.data.repository.entity.UsedeskArticleInfoOld
import ru.usedesk.knowledgebase_sdk.data.repository.entity.UsedeskCategoryOld
import ru.usedesk.knowledgebase_sdk.data.repository.entity.UsedeskSectionOld
import ru.usedesk.knowledgebase_sdk.entity.*
import toothpick.InjectConstructor
import javax.inject.Named

@InjectConstructor
internal class KnowledgeBaseInteractor(
        private val knowledgeRepository: IKnowledgeBaseRepository,
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

    override fun getSectionsRx(): Single<List<UsedeskSectionOld>> {
        return createSingle {
            it.onSuccess(getSections())
        }
    }

    override fun getArticleRx(articleId: Long): Single<UsedeskArticleBodyOld> {
        return createSingle {
            it.onSuccess(getArticle(articleId))
        }
    }

    override fun getCategoriesRx(sectionId: Long): Single<List<UsedeskCategoryOld>> {
        return createSingle {
            it.onSuccess(getCategories(sectionId))
        }
    }

    override fun getArticlesRx(categoryId: Long): Single<List<UsedeskArticleInfoOld>> {
        return createSingle {
            it.onSuccess(getArticles(categoryId))
        }
    }

    override fun getArticlesRx(searchQuery: String): Single<List<UsedeskArticleBodyOld>> {
        return createSingle {
            it.onSuccess(getArticles(searchQuery))
        }
    }

    override fun getArticlesRx(searchQuery: UsedeskSearchQueryOld): Single<List<UsedeskArticleBodyOld>> {
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

    @Throws(UsedeskException::class)
    override fun addViews(articleId: Long) {
        knowledgeRepository.addViews(configuration.accountId, configuration.token, articleId)
    }

    @Throws(UsedeskException::class)
    override fun getCategories(sectionId: Long): List<UsedeskCategoryOld> {
        return knowledgeRepository.getCategories(configuration.accountId, configuration.token, sectionId)
    }

    @Throws(UsedeskException::class)
    override fun getSections(): List<UsedeskSectionOld> {
        return knowledgeRepository.getSections(configuration.accountId, configuration.token)
    }

    @Throws(UsedeskException::class)
    override fun getArticle(articleId: Long): UsedeskArticleBodyOld {
        return knowledgeRepository.getArticleBody(configuration.accountId, configuration.token, articleId)
    }

    @Throws(UsedeskException::class)
    override fun getArticles(searchQuery: String): List<UsedeskArticleBodyOld> {
        val query = UsedeskSearchQueryOld.Builder(searchQuery).build()
        return knowledgeRepository.getArticles(configuration.accountId, configuration.token, query)
    }

    @Throws(UsedeskException::class)
    override fun getArticles(searchQuery: UsedeskSearchQueryOld): List<UsedeskArticleBodyOld> {
        return knowledgeRepository.getArticles(configuration.accountId, configuration.token, searchQuery)
    }

    @Throws(UsedeskException::class)
    override fun getArticles(categoryId: Long): List<UsedeskArticleInfoOld> {
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