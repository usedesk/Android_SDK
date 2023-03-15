package ru.usedesk.knowledgebase_gui.screens.main

import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.coroutines.launch
import ru.usedesk.common_gui.UsedeskViewModel
import ru.usedesk.knowledgebase_sdk.UsedeskKnowledgeBaseSdk
import ru.usedesk.knowledgebase_sdk.domain.IUsedeskKnowledgeBase
import ru.usedesk.knowledgebase_sdk.entity.UsedeskArticleContent
import ru.usedesk.knowledgebase_sdk.entity.UsedeskArticleInfo
import ru.usedesk.knowledgebase_sdk.entity.UsedeskCategory
import ru.usedesk.knowledgebase_sdk.entity.UsedeskSection

internal class KnowledgeBaseViewModel : UsedeskViewModel<KnowledgeBaseViewModel.State>(State()) {

    private val knowledgeBase = UsedeskKnowledgeBaseSdk.requireInstance() //TODO:inject

    init {
        mainScope.launch {
            knowledgeBase.modelFlow.collect { model -> onEvent(Event.KnowledgeBaseModel(model)) }
        }
    }

    fun ioEvent(io: () -> Event) {
        ioScope.launch {
            val event = io()
            mainScope.launch {
                onEvent(event)
            }
        }
    }

    fun onEvent(event: Event) {
        setModel {
            when (event) {
                is Event.KnowledgeBaseModel -> knowledgeBaseModel(event)
                is Event.SearchTextChanged -> searchTextChanged(event)
                is Event.SectionClicked -> sectionClicked(event)
                is Event.CategoryClicked -> categoryClicked(event)
                is Event.ArticleClicked -> articleClicked(event)
            }
        }
    }

    private fun State.knowledgeBaseModel(event: Event.KnowledgeBaseModel): State =
        when (currentScreen) {
            is State.Screen.Loading -> when (val sections = event.model.sections) {
                null -> copy(currentScreen = currentScreen.copy(loading = true))
                else -> copy(
                    currentScreen = State.Screen.Sections(sections)
                )
            }
            is State.Screen.Sections -> copy(
                currentScreen = currentScreen.copy(
                    sections = event.model.sections ?: currentScreen.sections
                )
            )
            else -> this
        }

    private fun State.searchTextChanged(event: Event.SearchTextChanged): State = copy(
        searchText = event.textFieldValue
    )

    private fun State.articleClicked(event: Event.ArticleClicked): State = copy(
        currentScreen = State.Screen.Article(
            currentScreen,
            event.article
        )
    )

    private fun State.categoryClicked(event: Event.CategoryClicked): State = copy(
        currentScreen = State.Screen.Articles(
            currentScreen,
            event.category
        )
    )

    private fun State.sectionClicked(event: Event.SectionClicked): State = copy(
        currentScreen = State.Screen.Categories(
            currentScreen,
            event.section
        )
    )

    private fun State.backPressed(): State? =
        when (val previousScreen = currentScreen.previousScreen) {
            null -> null
            else -> copy(currentScreen = previousScreen)
        }

    fun onBackPressed(): Boolean {
        var handled = false
        setModel {
            backPressed()?.also {
                handled = true
            } ?: this
        }
        return handled
    }

    override fun onCleared() {
        super.onCleared()

        UsedeskKnowledgeBaseSdk.release()
    }

    data class State(
        val currentScreen: Screen = Screen.Loading(),
        val searchText: TextFieldValue = TextFieldValue()
    ) {
        sealed interface Screen {
            val previousScreen: Screen?

            data class Loading(
                val loading: Boolean = true,
                val error: Boolean = false
            ) : Screen {
                override val previousScreen = null
            }

            data class Sections(val sections: List<UsedeskSection>) : Screen {
                override val previousScreen = null
            }

            data class Categories(
                override val previousScreen: Screen,
                val section: UsedeskSection
            ) : Screen

            data class Articles(
                override val previousScreen: Screen,
                val category: UsedeskCategory
            ) : Screen

            data class Article(
                override val previousScreen: Screen,
                val article: UsedeskArticleInfo,
                val articleContent: UsedeskArticleContent? = null
            ) : Screen
        }
    }

    sealed interface Event {
        data class KnowledgeBaseModel(val model: IUsedeskKnowledgeBase.Model) : Event
        data class SearchTextChanged(val textFieldValue: TextFieldValue) : Event
        data class SectionClicked(val section: UsedeskSection) : Event
        data class CategoryClicked(val category: UsedeskCategory) : Event
        data class ArticleClicked(val article: UsedeskArticleInfo) : Event
    }
}