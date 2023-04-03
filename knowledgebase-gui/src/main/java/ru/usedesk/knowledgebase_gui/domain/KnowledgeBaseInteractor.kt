package ru.usedesk.knowledgebase_gui.domain

import androidx.core.text.parseAsHtml
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.usedesk.common_sdk.UsedeskLog
import ru.usedesk.knowledgebase_gui._entity.LoadingState
import ru.usedesk.knowledgebase_gui._entity.RatingState
import ru.usedesk.knowledgebase_gui._entity.ReviewState
import ru.usedesk.knowledgebase_gui.domain.IKnowledgeBaseInteractor.*
import ru.usedesk.knowledgebase_sdk.data.repository.api.IUsedeskKnowledgeBase
import ru.usedesk.knowledgebase_sdk.data.repository.api.IUsedeskKnowledgeBase.*
import ru.usedesk.knowledgebase_sdk.entity.UsedeskArticleContent
import ru.usedesk.knowledgebase_sdk.entity.UsedeskSection
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
            val convertedData = (response as? GetSectionsResponse.Done)?.sections?.convert()
            sectionsModelFlow.updateWithLock {
                when (response) {
                    is GetSectionsResponse.Done -> {
                        val data = SectionsModel.Data(convertedData ?: listOf())
                        SectionsModel(
                            loadingState = LoadingState.Loaded(data = data),
                            data = data
                        )
                    }
                    is GetSectionsResponse.Error -> copy(
                        loadingState = LoadingState.Error(code = response.code)
                    )
                }
            }
        }
    }

    private fun List<UsedeskSection>.convert() = map { section ->
        section.copy(
            categories = section.categories.map { category ->
                category.copy(
                    description = category.description.htmlToStrings().joinToString("\n")
                )
            }
        )
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
        previous: List<ArticlesModel.SearchItem>?,
        page: Long
    ) {
        loadArticlesJob?.cancel()
        loadArticlesJob = ioScope.launch {
            val response = responseWithDelay(GetArticlesResponse.Error::class.java) {
                knowledgeRepository.getArticles(query, page)
            }
            articlesModelFlow.updateWithLock {
                when (response) {
                    is GetArticlesResponse.Done -> {
                        val newArticles = (previous ?: listOf()) + response.articles.toItems(
                            sectionsModelFlow.value.data
                        )
                        val articlesMap = newArticles.associateBy { it.item.id }
                        val totalArticles = newArticles
                            .toSet()
                            .filter { it.item.id in articlesMap }
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

    private fun List<UsedeskArticleContent>.toItems(
        sectionsData: SectionsModel.Data
    ) = map { item ->
        val category = sectionsData.categoriesMap[item.categoryId]
        val section = sectionsData.categoryParents[item.categoryId]
        ArticlesModel.SearchItem(
            item = item,
            sectionName = section?.title ?: "",
            categoryName = category?.title ?: "",
            description = item.text
                .htmlToStrings()
                .take(2)
                .joinToString("\n")
        )
    }

    private fun String.htmlToStrings() = parseAsHtml()
        .toString()
        .split('\n', '\r')
        .asSequence()
        .map {
            it.replace('\t', ' ')
                .replace("  ", "")
                .trim()
        }
        .filter { it.any(Char::isLetterOrDigit) }

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
                    is GetArticleResponse.Done -> {
                        launchAddViews(articleId)
                        ArticleModel(
                            articleId = articleId,
                            loadingState = LoadingState.Loaded(data = response.articleContent),
                            ratingState = ratingStateMap[articleId]
                                ?: RatingState.Required()
                        )
                    }
                    is GetArticleResponse.Error -> copy(
                        loadingState = LoadingState.Error(code = response.code)
                    )
                }
            }
        }
    }

    private fun launchAddViews(articleId: Long) {
        ioScope.launch {
            val response = knowledgeRepository.addViews(articleId)
            when (response) {
                is AddViewsResponse.Done -> {
                    UsedeskLog.onLog("AddViewsResponse.Done") { response.views.toString() }
                }
                is AddViewsResponse.Error -> {}
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

    private fun launchSendingRating(
        articleId: Long,
        good: Boolean
    ) {
        ioScope.launch {
            val response = responseWithDelay(SendRatingResponse.Error::class.java) {
                knowledgeRepository.sendRating(
                    articleId,
                    good
                )
            }
            articleModelFlow.updateWithLock {
                val ratingState = when (response) {
                    is SendRatingResponse.Done -> {
                        UsedeskLog.onLog("SendRatingResponse.Done") {
                            "positive: ${response.positive} negative: ${response.negative}"
                        }
                        RatingState.Sent(good)
                    }
                    is SendRatingResponse.Error -> RatingState.Required(good)
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
                if (this.articleId == articleId && ratingState is RatingState.Required) {
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
            val response = responseWithDelay(SendReviewResponse.Error::class.java) {
                knowledgeRepository.sendReview(
                    articleId,
                    message
                )
            }
            articleModelFlow.updateWithLock {
                copy(
                    reviewState = when (response) {
                        is SendReviewResponse.Done -> ReviewState.Sent
                        is SendReviewResponse.Error -> ReviewState.Required(true)
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
                if (this.articleId == articleId && reviewState is ReviewState.Required) {
                    launchSendingReview(articleId, message)
                    copy(reviewState = ReviewState.Sending)
                } else this
            }
        }
    }
}