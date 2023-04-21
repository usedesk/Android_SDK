
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
    fun loadSections(reload: Boolean = false): StateFlow<SectionsModel>

    fun loadArticles(
        newQuery: String? = null,
        nextPage: Boolean = false,
        reload: Boolean = false
    ): StateFlow<ArticlesModel>

    fun loadArticle(articleId: Long): StateFlow<ArticleModel>

    fun sendRating(
        articleId: Long,
        good: Boolean
    )

    fun sendReview(
        articleId: Long,
        subject: String,
        message: String
    )

    data class ArticleModel(
        val articleId: Long = 0L,
        val loadingState: LoadingState<UsedeskArticleContent> = LoadingState.Loading(),
        val ratingState: RatingState = RatingState.Required(),
        val reviewState: ReviewState = ReviewState.Required()
    )

    data class ArticlesModel(
        val query: String = "",
        val loadingState: LoadingState<List<SearchItem>> = LoadingState.Loading(),
        val searchItems: List<SearchItem>? = null,
        val page: Long = 1,
        val hasNextPage: Boolean = true
    ) {
        data class SearchItem(
            val item: UsedeskArticleContent,
            val sectionName: String,
            val categoryName: String,
            val description: String
        )
    }

    data class SectionsModel(
        val loadingState: LoadingState<Data> = LoadingState.Loading(),
        val data: Data = Data(listOf())
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
            val categoryParents: Map<Long, UsedeskSection> = sections.flatMap { section ->
                section.categories.map { it.id to section }
            }.toMap()
        }
    }
}