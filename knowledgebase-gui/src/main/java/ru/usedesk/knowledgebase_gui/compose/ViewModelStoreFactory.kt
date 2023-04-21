
package ru.usedesk.knowledgebase_gui.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner

internal class ViewModelStoreFactory {
    private val viewModelStoreMap = mutableMapOf<String, ViewModelStore>()

    fun get(key: String) = viewModelStoreMap.getOrPut(key) { ViewModelStore() }

    fun clear(key: String) {
        viewModelStoreMap[key]?.clear()
    }

    fun clearAll() {
        viewModelStoreMap.values.forEach(ViewModelStore::clear)
    }
}

@Composable
internal inline fun rememberViewModelStoreOwner(
    crossinline viewModelStoreProvider: @DisallowComposableCalls () -> ViewModelStore
) = remember {
    object : ViewModelStoreOwner {
        override fun getViewModelStore() = viewModelStoreProvider()
    }
}

internal enum class StoreKeys {
    LOADING,
    SECTIONS,
    CATEGORIES,
    ARTICLES,
    SEARCH,
    ARTICLE,
    REVIEW;

    override fun toString() = name
}