package ru.usedesk.knowledgebase_gui._entity

internal sealed interface LoadingState<DATA> {
    class Loading<DATA> : LoadingState<DATA>
    class Failed<DATA>(val code: Int?) : LoadingState<DATA>
    class Loaded<DATA>(val data: DATA) : LoadingState<DATA>
}