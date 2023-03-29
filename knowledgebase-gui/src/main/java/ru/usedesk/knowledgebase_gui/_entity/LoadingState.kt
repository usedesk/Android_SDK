package ru.usedesk.knowledgebase_gui._entity

internal sealed interface LoadingState<DATA> {
    data class Loading<DATA>(
        val loading: Boolean = true,
        val error: Boolean = false
    ) : LoadingState<DATA>

    class Loaded<DATA>(val data: DATA) : LoadingState<DATA>
}