package ru.usedesk.knowledgebase_gui.screens.main.blocks

import androidx.compose.ui.text.input.TextFieldValue
import ru.usedesk.common_gui.UsedeskViewModel
import ru.usedesk.knowledgebase_sdk.domain.IUsedeskKnowledgeBase
import ru.usedesk.knowledgebase_sdk.entity.UsedeskCategory
import ru.usedesk.knowledgebase_sdk.entity.UsedeskSection

internal class BlocksViewModel(
    private val knowledgeBase: IUsedeskKnowledgeBase
) : UsedeskViewModel<BlocksViewModel.State>(State()) {

    fun onSectionClick(section: UsedeskSection) {
        setModel {
            copy(
                page = State.Page.Categories(
                    previousPage = page,
                    sectionId = section.id
                )
            )
        }
    }

    fun onCategoryClick(category: UsedeskCategory) {
        setModel {
            copy(
                page = State.Page.Articles(
                    previousPage = page,
                    categoryId = category.id
                )
            )
        }
    }

    fun onSearchValue(value: TextFieldValue) {
        setModel { copy(searchText = value) }
    }

    data class State(
        val page: Page = Page.Sections,
        val searchText: TextFieldValue = TextFieldValue()
    ) {
        sealed interface Page {
            val previousPage: Page?

            object Sections : Page {
                override val previousPage = null
            }

            data class Categories(
                override val previousPage: Page,
                val sectionId: Long
            ) : Page

            data class Articles(
                override val previousPage: Page,
                val categoryId: Long
            ) : Page

            enum class Transition {
                NONE,
                STAY,
                FORWARD,
                BACKWARD
            }

            fun transition(previous: Page?) = when (previous) {
                null -> Transition.NONE
                else -> transitionMap[Pair(previous.javaClass, javaClass)]
                    ?: Transition.STAY
            }

            companion object {
                val transitionMap = listOf(
                    Sections::class.java to Categories::class.java,
                    Categories::class.java to Articles::class.java
                ).flatMap {
                    listOf(
                        Pair(it.first, it.second) to Transition.FORWARD,
                        Pair(it.second, it.first) to Transition.BACKWARD
                    )
                }.toMap()
            }
        }
    }
}