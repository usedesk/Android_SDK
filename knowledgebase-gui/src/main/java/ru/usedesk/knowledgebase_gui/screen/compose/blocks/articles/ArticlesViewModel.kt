
package ru.usedesk.knowledgebase_gui.screen.compose.blocks.articles

import androidx.compose.foundation.lazy.LazyListState
import ru.usedesk.common_gui.UsedeskViewModel
import ru.usedesk.knowledgebase_gui._entity.LoadingState
import ru.usedesk.knowledgebase_gui.domain.IKnowledgeBaseInteractor
import ru.usedesk.knowledgebase_gui.domain.IKnowledgeBaseInteractor.SectionsModel
import ru.usedesk.knowledgebase_gui.screen.compose.blocks.articles.ArticlesViewModel.State
import ru.usedesk.knowledgebase_sdk.entity.UsedeskArticleInfo

internal class ArticlesViewModel(
    private val kbInteractor: IKnowledgeBaseInteractor,
    private val categoryId: Long
) : UsedeskViewModel<State>(State()) {

    init {
        kbInteractor.loadSections().launchCollect { sectionsModel ->
            setModel {
                copy(
                    articles = (sectionsModel.loadingState as? LoadingState.Loaded<SectionsModel.Data>)
                        ?.data
                        ?.categoriesMap
                        ?.get(categoryId)
                        ?.articles
                        ?: articles
                )
            }
        }
    }


    data class State(
        val lazyListState: LazyListState = LazyListState(),
        val articles: List<UsedeskArticleInfo> = listOf()
    )
}