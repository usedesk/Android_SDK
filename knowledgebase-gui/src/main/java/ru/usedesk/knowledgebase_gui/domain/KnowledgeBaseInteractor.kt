
package ru.usedesk.knowledgebase_gui.domain

import androidx.core.text.parseAsHtml
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.usedesk.knowledgebase_gui._entity.LoadingState
import ru.usedesk.knowledgebase_gui._entity.LoadingState.Companion.ACCESS_DENIED
import ru.usedesk.knowledgebase_gui._entity.RatingState
import ru.usedesk.knowledgebase_gui._entity.ReviewState
import ru.usedesk.knowledgebase_gui.domain.IKnowledgeBaseInteractor.ArticleModel
import ru.usedesk.knowledgebase_gui.domain.IKnowledgeBaseInteractor.ArticlesModel
import ru.usedesk.knowledgebase_gui.domain.IKnowledgeBaseInteractor.SectionsModel
import ru.usedesk.knowledgebase_sdk.data.repository.api.IUsedeskKnowledgeBase
import ru.usedesk.knowledgebase_sdk.data.repository.api.IUsedeskKnowledgeBase.GetArticleResponse
import ru.usedesk.knowledgebase_sdk.data.repository.api.IUsedeskKnowledgeBase.GetArticlesResponse
import ru.usedesk.knowledgebase_sdk.data.repository.api.IUsedeskKnowledgeBase.GetSectionsResponse
import ru.usedesk.knowledgebase_sdk.data.repository.api.IUsedeskKnowledgeBase.SendRatingResponse
import ru.usedesk.knowledgebase_sdk.data.repository.api.IUsedeskKnowledgeBase.SendReviewResponse
import ru.usedesk.knowledgebase_sdk.entity.UsedeskArticleContent
import ru.usedesk.knowledgebase_sdk.entity.UsedeskSection
import javax.inject.Inject

internal class KnowledgeBaseInteractor @Inject constructor(
    private val knowledgeRepository: IUsedeskKnowledgeBase
) : IKnowledgeBaseInteractor {

    private val sectionsModelFlow = MutableStateFlow(SectionsModel())
    private val searchModelFlow = MutableStateFlow(ArticlesModel())
    private val articleModelFlowMap = mutableMapOf<Long, MutableStateFlow<ArticleModel>>()

    private val mutex = Mutex()
    private val ioScope = CoroutineScope(Dispatchers.IO)

    private val ratingStateMap = mutableMapOf<Long, RatingState>()

    private var loadSectionsJob: Job? = null
    private var loadArticlesJob: Job? = null
    private var loadArticleJob: Job? = null

    private fun getArticleModelFlow(articleId: Long) = articleModelFlowMap.getOrPut(articleId) {
        MutableStateFlow(ArticleModel())
    }

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
            searchModelFlow.updateWithLock {
                when (response) {
                    is GetArticlesResponse.Done -> {
                        val newSearchItems = (previous ?: listOf()) + response.articles.toItems(
                            sectionsModelFlow.value.data
                        )
                        val searchItemsMap = newSearchItems.associateBy { it.item.id }
                        val totalSearchItems = newSearchItems
                            .toSet()
                            .filter { it.item.id in searchItemsMap }
                        copy(
                            query = query,
                            loadingState = LoadingState.Loaded(
                                page = page,
                                data = totalSearchItems
                            ),
                            searchItems = totalSearchItems,
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
                .ifEmpty { "..." }
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
        searchModelFlow.updateWithLock {
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
                    else -> searchItems
                }
                launchArticlesJob(
                    query = query,
                    previous = previous,
                    page = newPage
                )
                copy(loadingState = LoadingState.Loading(page = newPage))
            } else this
        }
        searchModelFlow
    }

    private fun launchArticleJob(articleId: Long) {
        loadArticleJob?.cancel()
        loadArticleJob = ioScope.launch {
            val response = responseWithDelay(GetArticleResponse.Error::class.java) {
                knowledgeRepository.getArticle(articleId)
            }
            getArticleModelFlow(articleId).updateWithLock {
                when (response) {
                    is GetArticleResponse.Done -> {
                        if (response.articleContent.public) {
                            launchAddViews(articleId)
                            ArticleModel(
                                articleId = articleId,
                                loadingState = LoadingState.Loaded(data = response.articleContent.prepare()),
                                ratingState = ratingStateMap[articleId] ?: RatingState.Required()
                            )
                        } else copy(loadingState = LoadingState.Error(code = ACCESS_DENIED))
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
        }
    }

    override fun loadArticle(articleId: Long): StateFlow<ArticleModel> = runBlocking {
        getArticleModelFlow(articleId).apply {
            updateWithLock {
                if (this.articleId != articleId || loadingState is LoadingState.Error) {
                    val searchArticle = searchModelFlow.value.searchItems
                        ?.firstOrNull { it.item.id == articleId }
                    when (searchArticle) {
                        null -> {
                            launchArticleJob(articleId)
                            ArticleModel(articleId)
                        }
                        else -> ArticleModel(
                            articleId = articleId,
                            loadingState = LoadingState.Loaded(data = searchArticle.item.prepare()),
                            ratingState = ratingStateMap[articleId] ?: RatingState.Required()
                        )
                    }
                } else this
            }
        }
    }

    private fun UsedeskArticleContent.prepare() = copy(
        text = text.replace(
            "<table>",
            """<table bordercolor="black" border="1px" style="border-collapse: collapse; padding: 40px;">"""
        ).replace(
            "<th>",
            """<th style="padding: 4px;">"""
        ).replace(
            "<td>",
            """<td style="padding: 4px;">"""
        )
    )

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
            getArticleModelFlow(articleId).updateWithLock {
                val ratingState = when (response) {
                    is SendRatingResponse.Done -> RatingState.Sent(good)
                    is SendRatingResponse.Error -> RatingState.Required(good)
                }
                ratingStateMap[articleId] = ratingState
                copy(ratingState = ratingState)
            }
        }
    }

    override fun sendRating(
        articleId: Long,
        good: Boolean
    ) {
        runBlocking {
            getArticleModelFlow(articleId).updateWithLock {
                when (ratingState) {
                    is RatingState.Required -> {
                        val ratingState = RatingState.Sending(good)
                        ratingStateMap[articleId] = ratingState
                        launchSendingRating(articleId, good)
                        copy(ratingState = ratingState)
                    }
                    else -> this
                }
            }
        }
    }

    private fun launchSendingReview(
        articleId: Long,
        subject: String,
        message: String
    ) {
        ioScope.launch {
            val response = when {
                message.isEmpty() -> SendReviewResponse.Done()
                else -> responseWithDelay(SendReviewResponse.Error::class.java) {
                    knowledgeRepository.sendReview(subject, message)
                }
            }
            getArticleModelFlow(articleId).updateWithLock {
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
        subject: String,
        message: String
    ) {
        runBlocking {
            getArticleModelFlow(articleId).updateWithLock {
                when (reviewState) {
                    is ReviewState.Required -> {
                        launchSendingReview(articleId, subject, message)
                        copy(reviewState = ReviewState.Sending)
                    }
                    else -> this
                }
            }
        }
    }
}