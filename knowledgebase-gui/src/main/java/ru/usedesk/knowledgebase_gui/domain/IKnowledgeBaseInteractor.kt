package ru.usedesk.knowledgebase_gui.domain

import kotlinx.coroutines.flow.StateFlow
import ru.usedesk.knowledgebase_gui._entity.LoadingState
import ru.usedesk.knowledgebase_gui._entity.RatingState
import ru.usedesk.knowledgebase_gui._entity.ReviewState
import ru.usedesk.knowledgebase_sdk.entity.UsedeskArticleContent
import ru.usedesk.knowledgebase_sdk.entity.UsedeskArticleInfo
import ru.usedesk.knowledgebase_sdk.entity.UsedeskCategory
import ru.usedesk.knowledgebase_sdk.entity.UsedeskSection

internal interface IKnowledgeBaseInteractor {
    fun loadSections(): StateFlow<SectionsModel>

    fun loadArticles(
        query: String? = null,
        nextPage: Boolean = false,
        reload: Boolean = false
    ): StateFlow<ArticlesModel>

    fun loadArticle(articleId: Long): StateFlow<ArticleModel>

    fun addViews(articleId: Long)

    fun sendRating(
        articleId: Long,
        good: Boolean
    )

    fun sendReview(
        articleId: Long,
        message: String
    )

    data class ArticleModel(
        val articleId: Long = 0L,
        val loadingState: LoadingState<UsedeskArticleContent> = LoadingState.Loading(loading = false),
        val ratingState: RatingState = RatingState.Required,
        val reviewState: ReviewState = ReviewState.Required
    )

    data class ArticlesModel(
        val query: String = "",
        val loadingState: LoadingState<List<UsedeskArticleContent>> = LoadingState.Loading(loading = false),
        val page: Long = 0,
        val hasNextPage: Boolean = true
    )

    data class SectionsModel(
        val loadingState: LoadingState<Data> = LoadingState.Loading(loading = false)
    ) {
        data class Data(
            val sections: List<UsedeskSection>
        ) {
            val sectionsMap: Map<Long, UsedeskSection> = sections
                .associateBy(UsedeskSection::id)
            val categoriesMap: Map<Long, UsedeskCategory> = sections
                .flatMap(UsedeskSection::categories)
                .associateBy(UsedeskCategory::id)
            val articlesMap: Map<Long, UsedeskArticleInfo> = categoriesMap.values
                .flatMap(UsedeskCategory::articles)
                .associateBy(UsedeskArticleInfo::id)
        }
    }
}