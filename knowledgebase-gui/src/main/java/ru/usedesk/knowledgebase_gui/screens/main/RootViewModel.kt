package ru.usedesk.knowledgebase_gui.screens.main

import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.coroutines.launch
import ru.usedesk.common_gui.UsedeskViewModel
import ru.usedesk.knowledgebase_gui.screens.main.RootViewModel.State
import ru.usedesk.knowledgebase_sdk.UsedeskKnowledgeBaseSdk
import ru.usedesk.knowledgebase_sdk.domain.IUsedeskKnowledgeBase
import ru.usedesk.knowledgebase_sdk.entity.UsedeskArticleInfo

internal class RootViewModel(
    private val knowledgeBase: IUsedeskKnowledgeBase
) : UsedeskViewModel<State>(State()) {

    init {
        knowledgeBase.modelFlow.launchCollect { onEvent(Event.KnowledgeBaseModel(it)) }
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
                is Event.ArticleClicked -> articleClicked(event)
            }
        }
    }

    private fun State.knowledgeBaseModel(event: Event.KnowledgeBaseModel): State {
        val sections = event.model.sections
        return when (screen) {
            is State.Screen.Loading -> when (sections) {
                null -> copy(screen = screen.copy(loading = true))
                else -> copy(screen = State.Screen.Blocks)
            }
            else -> this
        }
    }

    private fun State.searchTextChanged(event: Event.SearchTextChanged): State = copy(
        searchText = event.textFieldValue
    )

    private fun State.articleClicked(event: Event.ArticleClicked): State = copy(
        screen = State.Screen.Article(
            previousScreen = screen,
            event.article.id
        )
    )

    private fun State.backPressed(): State? =
        when (val previousScreen = screen.previousScreen) {
            null -> null
            else -> copy(screen = previousScreen)
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
        val screen: Screen = Screen.Loading(),
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

            object Blocks : Screen {
                override val previousScreen = null
            }

            data class Article(
                override val previousScreen: Screen,
                val articleId: Long
            ) : Screen


            enum class Transition {
                NONE,
                STAY,
                FORWARD,
                BACKWARD
            }

            fun transition(previous: Screen?) = when (previous) {
                null -> Transition.NONE
                else -> transitionMap[Pair(previous.javaClass, javaClass)]
                    ?: Transition.STAY
            }

            companion object {
                val transitionMap = listOf(
                    Blocks::class.java to Article::class.java
                ).flatMap {
                    listOf(
                        Pair(it.first, it.second) to Transition.FORWARD,
                        Pair(it.second, it.first) to Transition.BACKWARD
                    )
                }.toMap()
            }
        }
    }

    sealed interface Event {
        data class KnowledgeBaseModel(val model: IUsedeskKnowledgeBase.Model) : Event
        data class SearchTextChanged(val textFieldValue: TextFieldValue) : Event
        data class ArticleClicked(val article: UsedeskArticleInfo) : Event
    }
}