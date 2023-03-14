package ru.usedesk.knowledgebase_gui.screens.main

import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.coroutines.launch
import ru.usedesk.common_gui.UsedeskViewModel
import ru.usedesk.knowledgebase_sdk.UsedeskKnowledgeBaseSdk
import ru.usedesk.knowledgebase_sdk.entity.UsedeskArticleContent
import ru.usedesk.knowledgebase_sdk.entity.UsedeskCategory
import ru.usedesk.knowledgebase_sdk.entity.UsedeskSection

internal class KnowledgeBaseViewModel : UsedeskViewModel<KnowledgeBaseViewModel.State>(State()) {

    private val knowledgeBase = UsedeskKnowledgeBaseSdk.requireInstance() //TODO:inject

    init {
        ioScope.launch { //TODO:
            val sections = knowledgeBase.getSections()
            setModel { copy(currentScreen = State.Screen.Sections(sections)) }
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
                is Event.SearchTextChanged -> searchTextChanged(event)
                is Event.SectionClicked -> sectionClicked(event)
                is Event.CategoryClicked -> categoryClicked(event)
                is Event.ArticleClicked -> articleClicked(event)
            }
        }
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

    private fun State.backPressed(): State? = when (currentScreen.previousScreen) {
        null -> null
        else -> copy(currentScreen = currentScreen.previousScreen)
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
        val currentScreen: Screen = Screen.Loading,
        val searchText: TextFieldValue = TextFieldValue()
    ) {
        sealed class Screen(val previousScreen: Screen? = null) {
            object Loading : Screen(null)

            class Sections(val sections: List<UsedeskSection>) : Screen(null)

            class Categories(
                previousScreen: Screen,
                val section: UsedeskSection
            ) : Screen(previousScreen)

            class Articles(
                previousScreen: Screen,
                val category: UsedeskCategory
            ) : Screen(previousScreen)

            class Article(
                previousScreen: Screen,
                val article: UsedeskArticleContent
            ) : Screen(previousScreen)
        }
    }

    sealed interface Event {
        data class SearchTextChanged(val textFieldValue: TextFieldValue) : Event
        data class SectionClicked(val section: UsedeskSection) : Event
        data class CategoryClicked(val category: UsedeskCategory) : Event
        data class ArticleClicked(val article: UsedeskArticleContent) : Event
    }
}