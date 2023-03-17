package ru.usedesk.knowledgebase_sdk.domain

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.usedesk.knowledgebase_sdk.data.repository.api.IKnowledgeBaseApi
import ru.usedesk.knowledgebase_sdk.data.repository.api.IKnowledgeBaseApi.GetArticleResponse
import ru.usedesk.knowledgebase_sdk.data.repository.api.IKnowledgeBaseApi.GetSectionsResponse
import ru.usedesk.knowledgebase_sdk.domain.IUsedeskKnowledgeBase.GetArticleResult
import ru.usedesk.knowledgebase_sdk.domain.IUsedeskKnowledgeBase.Model
import ru.usedesk.knowledgebase_sdk.entity.UsedeskArticleInfo
import ru.usedesk.knowledgebase_sdk.entity.UsedeskCategory
import ru.usedesk.knowledgebase_sdk.entity.UsedeskSection
import javax.inject.Inject

internal class KnowledgeBaseInteractor @Inject constructor(
    private val knowledgeApiRepository: IKnowledgeBaseApi
) : IUsedeskKnowledgeBase {

    override val modelFlow = MutableStateFlow(Model())

    private val mutex = Mutex()
    private val ioScope = CoroutineScope(Dispatchers.IO)
    private var loadSectionsJob: Job? = null

    init {
        runBlocking {
            mutex.withLock {
                launchSectionsJob()
            }
        }
    }

    private fun launchSectionsJob() {
        loadSectionsJob?.cancel()
        loadSectionsJob = ioScope.launch {
            val response = knowledgeApiRepository.getSections()
            mutex.withLock {
                updateModelLocked {
                    when (response) {
                        is GetSectionsResponse.Done -> {
                            val categories = response.sections
                                .flatMap(UsedeskSection::categories)
                            copy(
                                state = Model.State.LOADED,
                                sections = response.sections,
                                sectionsMap = response.sections
                                    .associateBy(UsedeskSection::id),
                                categoriesMap = categories
                                    .associateBy(UsedeskCategory::id),
                                articlesMap = categories
                                    .flatMap(UsedeskCategory::articles)
                                    .associateBy(UsedeskArticleInfo::id)
                            )
                        }
                        is GetSectionsResponse.Error -> copy(
                            state = Model.State.FAILED
                        )
                    }
                }
            }
        }
    }

    private fun updateModelLocked(update: Model.() -> Model) = modelFlow.value.update()
        .also { modelFlow.value = it }

    override fun loadSections(reload: Boolean) {
        runBlocking {
            mutex.withLock {
                val model = modelFlow.value
                if (model.sections == null || reload) {
                    updateModelLocked { copy(state = Model.State.LOADING) }
                    launchSectionsJob()
                }
            }
        }
    }

    override fun getArticle(
        articleId: Long,
        onResult: (result: GetArticleResult) -> Unit
    ) {
        ioScope.launch {
            val response = knowledgeApiRepository.getArticle(articleId)
            onResult(
                when (response) {
                    is GetArticleResponse.Done -> GetArticleResult.Done(response.articleContent)
                    is GetArticleResponse.Error -> GetArticleResult.Error(response.code)
                }
            )
        }
    }

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