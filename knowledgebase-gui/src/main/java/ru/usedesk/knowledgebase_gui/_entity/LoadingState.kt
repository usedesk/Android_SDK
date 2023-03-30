package ru.usedesk.knowledgebase_gui._entity

internal sealed interface LoadingState<DATA> {
    val data: DATA?

    class Loading<DATA>(override val data: DATA? = null) : LoadingState<DATA>

    class Error<DATA>(
        override val data: DATA? = null,
        val code: Int? = null
    ) : LoadingState<DATA>

    class Loaded<DATA>(override val data: DATA) : LoadingState<DATA>
}