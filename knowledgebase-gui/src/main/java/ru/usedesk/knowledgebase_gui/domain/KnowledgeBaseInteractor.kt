package ru.usedesk.knowledgebase_gui.domain

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.usedesk.knowledgebase_gui._entity.LoadingState
import ru.usedesk.knowledgebase_gui._entity.RatingState
import ru.usedesk.knowledgebase_gui._entity.ReviewState
import ru.usedesk.knowledgebase_gui.domain.IKnowledgeBaseInteractor.*
import ru.usedesk.knowledgebase_sdk.data.repository.api.IUsedeskKnowledgeBase
import ru.usedesk.knowledgebase_sdk.data.repository.api.IUsedeskKnowledgeBase.*
import javax.inject.Inject

internal class KnowledgeBaseInteractor @Inject constructor(
    private val knowledgeRepository: IUsedeskKnowledgeBase
) : IKnowledgeBaseInteractor {

    override val sectionsModelFlow = MutableStateFlow(SectionsModel())
    override val articlesModelFlow = MutableStateFlow(ArticlesModel())
    override val articleModelFlow = MutableStateFlow(ArticleModel())

    private val mutex = Mutex()
    private val ioScope = CoroutineScope(Dispatchers.IO)

    private val ratingStateMap = mutableMapOf<Long, RatingState>()

    private var loadSectionsJob: Job? = null
    private var loadArticlesJob: Job? = null
    private var loadArticleJob: Job? = null

    init {
        loadSections(true)
    }

    private fun launchSectionsJob() {
        loadSectionsJob?.cancel()
        loadSectionsJob = ioScope.launch {
            val response = knowledgeRepository.getSections()
            sectionsModelFlow.updateWithLock {
                when (response) {
                    is GetSectionsResponse.Done -> SectionsModel(
                        loadingState = LoadingState.Loaded(SectionsModel.Data(response.sections))
                    )
                    is GetSectionsResponse.Error -> copy(
                        loadingState = LoadingState.Failed(response.code)
                    )
                }
            }
        }
    }

    private fun <T> MutableStateFlow<T>.update(onUpdate: T.() -> T) =
        onUpdate(value).also { value = it }

    private suspend fun <T> MutableStateFlow<T>.updateWithLock(onUpdate: T.() -> T) =
        mutex.withLock { update(onUpdate) }

    override fun loadSections(reload: Boolean) {
        runBlocking {
            sectionsModelFlow.updateWithLock {
                if (loadingState is LoadingState.Failed || reload) {
                    launchSectionsJob()
                    copy(loadingState = LoadingState.Loading())
                } else this
            }
        }
    }

    private fun launchArticlesJob(query: String) {
        loadArticlesJob?.cancel()
        loadArticlesJob = ioScope.launch {
            val response = knowledgeRepository.getArticles(query)
            articlesModelFlow.updateWithLock {
                when (response) {
                    is GetArticlesResponse.Done -> ArticlesModel(
                        query = query,
                        loadingState = LoadingState.Loaded(response.articles) //TODO: проверить, нужна ли здесь пагинация
                    )
                    is GetArticlesResponse.Error -> copy(
                        loadingState = LoadingState.Failed(response.code)
                    )
                }
            }
        }
    }

    override fun loadArticles(
        query: String,
        reload: Boolean
    ) {
        runBlocking {
            articlesModelFlow.updateWithLock {
                if (reload ||
                    this.query != query ||
                    loadingState is LoadingState.Failed
                ) {
                    launchArticlesJob(query)
                    copy(
                        loadingState = LoadingState.Loading(),
                        query = query
                    )
                } else this
            }
        }
    }

    private fun launchArticleJob(articleId: Long) {
        loadArticleJob?.cancel()
        loadArticleJob = ioScope.launch {
            val response = knowledgeRepository.getArticle(articleId)
            articleModelFlow.updateWithLock {
                when (response) {
                    is GetArticleResponse.Done -> ArticleModel(
                        articleId = articleId,
                        loadingState = LoadingState.Loaded(response.articleContent),
                        ratingState = ratingStateMap[articleId]
                            ?: RatingState.Required
                    )
                    is GetArticleResponse.Error -> copy(
                        loadingState = LoadingState.Failed(response.code)
                    )
                }
            }
        }
    }

    override fun loadArticle(articleId: Long) {
        runBlocking {
            articleModelFlow.updateWithLock {
                if (this.articleId != articleId ||
                    loadingState !is LoadingState.Loading
                ) {
                    launchArticleJob(articleId)
                    ArticleModel(articleId)
                } else this
            }
        }
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
            delay(10000) //TODO
            val response = knowledgeRepository.sendRating(
                articleId,
                good
            )
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
            delay(10000) //TODO
            val response = knowledgeRepository.sendReview(
                articleId,
                message
            )
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