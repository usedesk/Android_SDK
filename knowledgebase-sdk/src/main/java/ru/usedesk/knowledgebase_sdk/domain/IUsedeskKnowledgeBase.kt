package ru.usedesk.knowledgebase_sdk.domain

import kotlinx.coroutines.flow.StateFlow
import ru.usedesk.common_sdk.entity.exceptions.UsedeskException
import ru.usedesk.knowledgebase_sdk.entity.UsedeskArticleContent
import ru.usedesk.knowledgebase_sdk.entity.UsedeskSection

interface IUsedeskKnowledgeBase {
    val modelFlow: StateFlow<Model>

    fun loadSections(reload: Boolean = false)

    @Throws(UsedeskException::class)
    fun getArticle(articleId: Long): UsedeskArticleContent

    @Throws(UsedeskException::class)
    fun addViews(articleId: Long)

    @Throws(UsedeskException::class)
    fun sendRating(articleId: Long, good: Boolean)

    @Throws(UsedeskException::class)
    fun sendRating(articleId: Long, message: String)

    data class Model(
        val state: State = State.LOADING,
        val sections: List<UsedeskSection>? = null
    ) {
        enum class State {
            LOADING,
            LOADED,
            FAILED
        }
    }
}