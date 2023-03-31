package ru.usedesk.knowledgebase_gui.domain

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.usedesk.knowledgebase_gui._entity.LoadingState
import ru.usedesk.knowledgebase_gui._entity.RatingState
import ru.usedesk.knowledgebase_gui._entity.ReviewState
import ru.usedesk.knowledgebase_gui.domain.IKnowledgeBaseInteractor.*
import ru.usedesk.knowledgebase_sdk.data.repository.api.IUsedeskKnowledgeBase
import ru.usedesk.knowledgebase_sdk.data.repository.api.IUsedeskKnowledgeBase.*
import ru.usedesk.knowledgebase_sdk.entity.UsedeskArticleContent
import javax.inject.Inject

internal class KnowledgeBaseInteractor @Inject constructor(
    private val knowledgeRepository: IUsedeskKnowledgeBase
) : IKnowledgeBaseInteractor {

    private val sectionsModelFlow = MutableStateFlow(SectionsModel())
    private val articlesModelFlow = MutableStateFlow(ArticlesModel())
    private val articleModelFlow = MutableStateFlow(ArticleModel())

    private val mutex = Mutex()
    private val ioScope = CoroutineScope(Dispatchers.IO)

    private val ratingStateMap = mutableMapOf<Long, RatingState>()

    private var loadSectionsJob: Job? = null
    private var loadArticlesJob: Job? = null
    private var loadArticleJob: Job? = null

    private fun <T> MutableStateFlow<T>.update(onUpdate: T.() -> T) =
        onUpdate(value).also { value = it }

    private suspend fun <T> MutableStateFlow<T>.updateWithLock(onUpdate: T.() -> T) =
        mutex.withLock { update(onUpdate) }

    private fun launchSectionsJob() {
        loadSectionsJob?.cancel()
        loadSectionsJob = ioScope.launch {
            val response = responseWithDelay(GetSectionsResponse.Error::class.java) {
                knowledgeRepository.getSections()
            }
            sectionsModelFlow.updateWithLock {
                when (response) {
                    is GetSectionsResponse.Done -> SectionsModel(
                        loadingState = LoadingState.Loaded(data = SectionsModel.Data(response.sections))
                    )
                    is GetSectionsResponse.Error -> copy(
                        loadingState = LoadingState.Error(code = response.code)
                    )
                }
            }
        }
    }

    override fun loadSections(reload: Boolean): StateFlow<SectionsModel> = runBlocking {
        sectionsModelFlow.updateWithLock {
            if (reload || loadingState is LoadingState.Error) {
                launchSectionsJob()
                copy(loadingState = LoadingState.Loading())
            } else this
        }
        sectionsModelFlow
    }

    private fun launchArticlesJob(
        query: String,
        previous: List<UsedeskArticleContent>?,
        page: Long
    ) {
        loadArticlesJob?.cancel()
        loadArticlesJob = ioScope.launch {
            val response = responseWithDelay(GetArticlesResponse.Error::class.java) {
                knowledgeRepository.getArticles(query, page)
            }
            delay(3000)
            articlesModelFlow.updateWithLock {
                when (response) {
                    is GetArticlesResponse.Done -> {
                        val newArticles = (previous ?: listOf()) + response.articles
                        val articlesMap = newArticles.associateBy(UsedeskArticleContent::id)
                        val totalArticles = newArticles
                            .toSet()
                            .filter { it.id in articlesMap }
                        copy(
                            query = query,
                            loadingState = LoadingState.Loaded(
                                page = page,
                                data = totalArticles
                            ),
                            articles = totalArticles,
                            page = page,
                            hasNextPage = response.articles.isNotEmpty()
                        )
                    }
                    is GetArticlesResponse.Error -> copy(
                        loadingState = LoadingState.Error(
                            page = page,
                            code = response.code
                        )
                    )
                }
            }
        }
    }

    override fun loadArticles(
        newQuery: String?,
        nextPage: Boolean,
        reload: Boolean
    ): StateFlow<ArticlesModel> = runBlocking {
        articlesModelFlow.updateWithLock {
            val query = newQuery ?: this.query
            val newLoad = reload || this.query != query
            if (newLoad ||
                nextPage && hasNextPage && loadingState !is LoadingState.Loading ||
                loadingState is LoadingState.Error
            ) {
                val newPage = when {
                    newLoad -> 1
                    else -> page + 1
                }
                val previous = when {
                    newLoad -> null
                    else -> articles
                }
                launchArticlesJob(
                    query = query,
                    previous = previous,
                    page = newPage
                )
                copy(loadingState = LoadingState.Loading(page = newPage))
            } else this
        }
        articlesModelFlow
    }

    private fun launchArticleJob(articleId: Long) {
        loadArticleJob?.cancel()
        loadArticleJob = ioScope.launch {
            val response = responseWithDelay(GetArticleResponse.Error::class.java) {
                knowledgeRepository.getArticle(articleId)
            }
            articleModelFlow.updateWithLock {
                when (response) {
                    is GetArticleResponse.Done -> ArticleModel(
                        articleId = articleId,
                        loadingState = LoadingState.Loaded(data = response.articleContent),
                        ratingState = ratingStateMap[articleId]
                            ?: RatingState.Required
                    )
                    is GetArticleResponse.Error -> copy(
                        loadingState = LoadingState.Error(code = response.code)
                    )
                }
            }
        }
    }

    override fun loadArticle(articleId: Long): StateFlow<ArticleModel> = runBlocking {
        articleModelFlow.updateWithLock {
            if (this.articleId != articleId || loadingState is LoadingState.Error) {
                launchArticleJob(articleId)
                ArticleModel(articleId)
            } else this
        }
        articleModelFlow
    }

    override fun addViews(articleId: Long) {
        ioScope.launch {
            knowledgeRepository.addViews(articleId)
        }
    }

    private fun launchSendingRating(
        articleId: Long,
        good: Boolean
    ) {
        ioScope.launch {
            val response = responseWithDelay(SendResponse.Error::class.java) {
                knowledgeRepository.sendRating(
                    articleId,
                    good
                )
            }
            articleModelFlow.updateWithLock {
                val ratingState = when (response) {
                    SendResponse.Done -> RatingState.Sent(good)
                    is SendResponse.Error -> RatingState.Required
                }
                ratingStateMap[articleId] = ratingState
                when (articleId) {
                    this.articleId -> copy(ratingState = ratingState)
                    else -> this
                }
            }
        }
    }

    override fun sendRating(
        articleId: Long,
        good: Boolean
    ) {
        runBlocking {
            articleModelFlow.updateWithLock {
                if (this.articleId == articleId &&
                    ratingState == RatingState.Required
                ) {
                    val ratingState = RatingState.Sending(good)
                    ratingStateMap[articleId] = ratingState
                    launchSendingRating(articleId, good)
                    copy(ratingState = ratingState)
                } else this
            }
        }
    }

    private fun launchSendingReview(
        articleId: Long,
        message: String
    ) {
        ioScope.launch {
            val response = responseWithDelay(SendResponse.Error::class.java) {
                knowledgeRepository.sendReview(
                    articleId,
                    message
                )
            }
            articleModelFlow.updateWithLock {
                copy(
                    reviewState = when (response) {
                        SendResponse.Done -> ReviewState.Sent
                        is SendResponse.Error -> ReviewState.Failed(response.code)
                    }
                )
            }
        }
    }

    private suspend fun <RESPONSE : Any> responseWithDelay(
        errorClass: Class<out RESPONSE>,
        getResponse: () -> RESPONSE
    ): RESPONSE {
        val delayJob = ioScope.async { delay(1000) }
        return getResponse().also { response ->
            if (response::class.java == errorClass) {
                delayJob.await()
            }
        }
    }

    override fun sendReview(
        articleId: Long,
        message: String
    ) {
        runBlocking {
            articleModelFlow.updateWithLock {
                if (this.articleId == articleId &&
                    reviewState == ReviewState.Required
                ) {
                    launchSendingReview(articleId, message)
                    copy(reviewState = ReviewState.Sending)
                } else this
            }
        }
    }
}