package ru.usedesk.knowledgebase_gui.compose

import androidx.lifecycle.ViewModelStore

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