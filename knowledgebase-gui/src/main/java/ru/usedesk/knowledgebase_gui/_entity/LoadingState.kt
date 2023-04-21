
package ru.usedesk.knowledgebase_gui._entity

internal sealed interface LoadingState<DATA> {
    val page: Long

    class Loading<DATA>(
        override val page: Long = 1L
    ) : LoadingState<DATA>

    class Error<DATA>(
        override val page: Long = 1L,
        val code: Int? = null
    ) : LoadingState<DATA>

    class Loaded<DATA>(
        override val page: Long = 1L,
        val data: DATA
    ) : LoadingState<DATA>

    companion object {
        const val ACCESS_DENIED = -1
    }
}