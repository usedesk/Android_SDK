package ru.usedesk.knowledgebase_gui.screen

import androidx.compose.ui.text.input.TextFieldValue
import ru.usedesk.common_gui.UsedeskViewModel
import ru.usedesk.common_sdk.entity.UsedeskEvent
import ru.usedesk.knowledgebase_gui._di.KbUiComponent
import ru.usedesk.knowledgebase_gui._entity.LoadingState
import ru.usedesk.knowledgebase_gui.domain.IKnowledgeBaseInteractor
import ru.usedesk.knowledgebase_gui.domain.IKnowledgeBaseInteractor.SectionsModel
import ru.usedesk.knowledgebase_gui.screen.RootViewModel.State
import ru.usedesk.knowledgebase_sdk.entity.UsedeskCategory
import ru.usedesk.knowledgebase_sdk.entity.UsedeskSection

internal class RootViewModel(
    private val kbInteractor: IKnowledgeBaseInteractor
) : UsedeskViewModel<State>(State()) {

    init {
        kbInteractor.loadSections().launchCollect { onEvent(Event.KbSectionsModel(it)) }
    }

    fun onEvent(event: Event) {
        setModel {
            when (event) {
                is Event.TryAgain -> tryAgain()
                is Event.KbSectionsModel -> kbSectionsModel(event)
                is Event.SearchTextChanged -> searchTextChanged(event)
                is Event.SearchClicked -> searchClicked()
                is Event.SearchClearClicked -> searchClearClicked()
                is Event.SearchCancelClicked -> searchCancelClicked()
                is Event.SectionClicked -> sectionClicked(event)
                is Event.CategoryClicked -> categoryClicked(event)
                is Event.ArticleClicked -> articleClicked(event)
                is Event.GoReview -> articleRatingClicked(event)
            }
        }.useAction()
    }

    private fun State.backPressed() = when (screen) {
        is State.Screen.Blocks -> when (val previousBlock = blocksState.block.previousBlock) {
            null -> null
            else -> copy(
                blocksState = blocksState.copy(
                    block = previousBlock,
                    searchText = TextFieldValue(),
                    clearFocus = UsedeskEvent(Unit)
                )
            )
        }
        else -> null
    } ?: when (val previousScreen = screen.previousScreen) {
        null -> null
        else -> copy(screen = previousScreen)
    }

    fun onBackPressed(): Boolean {
        var handled = false
        setModel {
            backPressed()
                ?.copy(clearFocus = UsedeskEvent(Unit))
                ?.also { handled = true }
                ?: this
        }
        return handled
    }

    private fun State.tryAgain(): State = copy(
        action = UsedeskEvent {
            kbInteractor.loadSections()
        }
    )

    private fun State.searchClicked(): State = copy(
        blocksState = blocksState.copy(
            block = when (blocksState.block) {
                is State.BlocksState.Block.Search -> blocksState.block
                else -> State.BlocksState.Block.Search(blocksState.block)
            }
        ),
        clearFocus = UsedeskEvent(Unit),
        action = UsedeskEvent {
            kbInteractor.loadArticles(
                newQuery = blocksState.searchText.text,
                nextPage = false,
                reload = true
            )
        }
    )

    private fun State.searchClearClicked(): State = copy(
        blocksState = blocksState.copy(searchText = TextFieldValue()),
        clearFocus = UsedeskEvent(Unit),
        action = UsedeskEvent {
            kbInteractor.loadArticles(
                newQuery = "",
                nextPage = false,
                reload = true
            )
        }
    )


    private fun State.searchCancelClicked(): State = copy(
        blocksState = blocksState.copy(
            searchText = TextFieldValue(),
            block = blocksState.block.previousBlock ?: State.BlocksState.Block.Sections
        ),
        clearFocus = UsedeskEvent(Unit)
    )

    private fun State.kbSectionsModel(event: Event.KbSectionsModel): State = when (screen) {
        is State.Screen.Loading -> when (event.sectionsModel.loadingState) {
            is LoadingState.Loaded -> copy(screen = State.Screen.Blocks)
            else -> this
        }
        else -> this
    }


    private fun State.searchTextChanged(event: Event.SearchTextChanged): State = copy(
        blocksState = blocksState.copy(searchText = event.value)
    )

    private fun State.sectionClicked(event: Event.SectionClicked): State = copy(
        blocksState = blocksState.copy(
            block = State.BlocksState.Block.Categories(
                previousBlock = blocksState.block,
                title = event.section.title,
                sectionId = event.section.id
            )
        )
    )

    private fun State.categoryClicked(event: Event.CategoryClicked): State = copy(
        blocksState = blocksState.copy(
            block = State.BlocksState.Block.Articles(
                previousBlock = blocksState.block,
                title = event.category.title,
                categoryId = event.category.id
            )
        )
    )

    private fun State.articleClicked(event: Event.ArticleClicked): State = copy(
        screen = State.Screen.Article(
            previousScreen = screen,
            title = event.articleTitle,
            articleId = event.articleId
        )
    )

    private fun State.articleRatingClicked(event: Event.GoReview): State = copy(
        screen = State.Screen.Review(screen, event.articleId)
    )

    override fun onCleared() {
        super.onCleared()

        KbUiComponent.close()
    }

    data class State(
        val screen: Screen = Screen.Loading,
        val blocksState: BlocksState = BlocksState(),
        val clearFocus: UsedeskEvent<Unit>? = null,
        private val action: UsedeskEvent<() -> Unit>? = null
    ) {

        fun useAction() = action?.use { it() }

        sealed interface Screen {
            val previousScreen: Screen?

            object Loading : Screen {
                override val previousScreen = null
            }

            object Blocks : Screen {
                override val previousScreen = null
            }

            data class Article(
                override val previousScreen: Screen,
                val title: String,
                val articleId: Long
            ) : Screen

            data class Review(
                override val previousScreen: Screen,
                val articleId: Long
            ) : Screen


            fun transition(previous: Screen?) = when (previous) {
                null -> Transition.NONE
                else -> transitionMap[Pair(previous.javaClass, javaClass)]
                    ?: Transition.NONE
            }

            companion object {
                val transitionMap = Transition.getTransitionMap(
                    Blocks::class.java to Article::class.java,
                    Article::class.java to Review::class.java
                )
            }
        }

        data class BlocksState(
            val block: Block = Block.Sections,
            val searchText: TextFieldValue = TextFieldValue(),
            val clearFocus: UsedeskEvent<Unit>? = null
        ) {
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

                data class Search(
                    override val previousBlock: Block
                ) : Block {
                    override val title: String = ""
                }

                fun transition(previous: Block?) = when (previous) {
                    null -> Transition.NONE
                    else -> transitionMap[Pair(previous.javaClass, javaClass)]
                        ?: Transition.NONE
                }

                companion object {
                    val transitionMap = Transition.getTransitionMap(
                        Sections::class.java to Categories::class.java,
                        Categories::class.java to Articles::class.java,
                        Sections::class.java to Search::class.java,
                        Categories::class.java to Search::class.java,
                        Articles::class.java to Search::class.java
                    )
                }
            }
        }

        enum class Transition {
            NONE,
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
    }

    sealed interface Event {
        object TryAgain : Event
        data class KbSectionsModel(val sectionsModel: SectionsModel) : Event
        data class SearchTextChanged(val value: TextFieldValue) : Event
        object SearchClicked : Event
        object SearchCancelClicked : Event
        object SearchClearClicked : Event
        data class SectionClicked(val section: UsedeskSection) : Event
        data class CategoryClicked(val category: UsedeskCategory) : Event
        data class GoReview(val articleId: Long) : Event

        data class ArticleClicked(
            val articleId: Long,
            val articleTitle: String
        ) : Event
    }
}