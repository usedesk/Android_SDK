
package ru.usedesk.knowledgebase_gui._entity

internal sealed interface ContentState<DATA> {
    class Empty<DATA> : ContentState<DATA>
    data class Error<DATA>(val code: Int? = null) : ContentState<DATA>
    data class Loaded<DATA>(val content: DATA) : ContentState<DATA>

    fun <OLD_DATA> update(
        loadingState: LoadingState<OLD_DATA>,
        convert: OLD_DATA.() -> DATA
    ): ContentState<DATA> = when (loadingState) {
        is LoadingState.Loading -> this
        is LoadingState.Error -> Error(loadingState.code)
        is LoadingState.Loaded -> Loaded(loadingState.data.convert())
    }
}