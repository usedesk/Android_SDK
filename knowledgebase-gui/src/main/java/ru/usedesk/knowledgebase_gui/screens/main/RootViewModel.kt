package ru.usedesk.knowledgebase_gui.screens.main

import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.coroutines.launch
import ru.usedesk.common_gui.UsedeskViewModel
import ru.usedesk.common_sdk.entity.UsedeskEvent
import ru.usedesk.knowledgebase_gui.screens.main.RootViewModel.State
import ru.usedesk.knowledgebase_gui.screens.main.RootViewModel.State.Transition.Companion.getTransitionMap
import ru.usedesk.knowledgebase_sdk.UsedeskKnowledgeBaseSdk
import ru.usedesk.knowledgebase_sdk.domain.IUsedeskKnowledgeBase
import ru.usedesk.knowledgebase_sdk.entity.UsedeskArticleInfo
import ru.usedesk.knowledgebase_sdk.entity.UsedeskCategory
import ru.usedesk.knowledgebase_sdk.entity.UsedeskSection

internal class RootViewModel : UsedeskViewModel<State>(State()) {

    private val knowledgeBase = UsedeskKnowledgeBaseSdk.requireInstance()

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
                is Event.SectionClicked -> sectionClicked(event)
                is Event.CategoryClicked -> categoryClicked(event)
                is Event.ArticleClicked -> articleClicked(event)
            }
        }
    }

    private fun State.backPressed() = when (screen) {
        is State.Screen.Blocks -> when (val previousBlock = screen.block.previousBlock) {
            null -> null
            else -> copy(screen = screen.copy(block = previousBlock))
        }
        else -> null
    } ?: when (val previousScreen = screen.previousScreen) {
        null -> null
        else -> copy(screen = previousScreen)
    }

    fun onBackPressed(): Boolean {
        var handled = false
        setModel {
            backPressed()?.also { handled = true } ?: this
        }
        return handled
    }

    private fun State.knowledgeBaseModel(event: Event.KnowledgeBaseModel): State {
        val sections = event.model.sections
        return when (screen) {
            is State.Screen.Loading -> when (sections) {
                null -> copy(screen = screen.copy(loading = true))
                else -> copy(screen = State.Screen.Blocks())
            }
            else -> this
        }
    }

    private fun State.searchTextChanged(event: Event.SearchTextChanged): State = when (screen) {
        is State.Screen.Blocks -> copy(
            screen = screen.copy(searchText = event.value)
        )
        else -> this
    }

    private fun State.sectionClicked(event: Event.SectionClicked): State = when (screen) {
        is State.Screen.Blocks -> copy(
            screen = screen.copy(
                block = State.Screen.Blocks.Block.Categories(
                    previousBlock = screen.block,
                    title = event.section.title,
                    sectionId = event.section.id
                )
            )
        )
        else -> this
    }


    private fun State.categoryClicked(event: Event.CategoryClicked): State = when (screen) {
        is State.Screen.Blocks -> copy(
            screen = screen.copy(
                block = State.Screen.Blocks.Block.Articles(
                    previousBlock = screen.block,
                    title = event.category.title,
                    categoryId = event.category.id
                )
            )
        )
        else -> this
    }

    private fun State.articleClicked(event: Event.ArticleClicked): State = copy(
        screen = State.Screen.Article(
            previousScreen = screen,
            title = event.article.title,
            articleId = event.article.id
        )
    )

    override fun onCleared() {
        super.onCleared()

        UsedeskKnowledgeBaseSdk.release()
    }

    data class State(
        val screen: Screen = Screen.Loading(),
        val goBack: UsedeskEvent<Unit>? = null
    ) {

        enum class Transition {
            NONE,
            STAY,
            FORWARD,
            BACKWARD;

            companion object {
                fun <T> getTransitionMap(vararg pairs: Pair<T, T>) = pairs.flatMap {
                    listOf(
                        Pair(it.first, it.second) to FORWARD,
                        Pair(it.second, it.first) to BACKWARD
                    )
                }.toMap()
            }
        }

        sealed interface Screen {
            val previousScreen: Screen?
            val title: String?

            data class Loading(
                val loading: Boolean = true,
                val error: Boolean = false
            ) : Screen {
                override val previousScreen = null
                override val title = null
            }

            data class Blocks(
                val block: Block = Block.Sections,
                val searchText: TextFieldValue = TextFieldValue()
            ) : Screen {
                override val previousScreen = null
                override val title = block.title

                sealed interface Block {
                    val previousBlock: Block?
                    val title: String?

                    object Sections : Block {
                        override val previousBlock = null
                        override val title = null
                    }

                    data class Categories(
                        override val previousBlock: Block,
                        override val title: String,
                        val sectionId: Long
                    ) : Block

                    data class Articles(
                        override val previousBlock: Block,
                        override val title: String,
                        val categoryId: Long
                    ) : Block

                    fun transition(previous: Block?) = when (previous) {
                        null -> Transition.NONE
                        else -> transitionMap[Pair(previous.javaClass, javaClass)]
                            ?: Transition.NONE
                    }

                    companion object {
                        val transitionMap = getTransitionMap(
                            Sections::class.java to Categories::class.java,
                            Categories::class.java to Articles::class.java
                        )
                    }
                }
            }

            data class Article(
                override val previousScreen: Screen,
                override val title: String,
                val articleId: Long
            ) : Screen


            fun transition(previous: Screen?) = when (previous) {
                null -> Transition.NONE
                else -> transitionMap[Pair(previous.javaClass, javaClass)]
                    ?: Transition.NONE
            }

            companion object {
                val transitionMap = getTransitionMap(
                    Blocks::class.java to Article::class.java
                )
            }
        }
    }

    sealed interface Event {
        data class KnowledgeBaseModel(val model: IUsedeskKnowledgeBase.Model) : Event
        data class SearchTextChanged(val value: TextFieldValue) : Event
        data class SectionClicked(val section: UsedeskSection) : Event
        data class CategoryClicked(val category: UsedeskCategory) : Event
        data class ArticleClicked(val article: UsedeskArticleInfo) : Event
    }
}