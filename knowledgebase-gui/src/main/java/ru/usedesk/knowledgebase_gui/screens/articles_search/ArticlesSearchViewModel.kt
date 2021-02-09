package ru.usedesk.knowledgebase_gui.screens.articles_search

import androidx.lifecycle.MutableLiveData
import ru.usedesk.common_gui.UsedeskViewModel
import ru.usedesk.knowledgebase_sdk.UsedeskKnowledgeBaseSdk
import ru.usedesk.knowledgebase_sdk.entity.UsedeskArticleContent

internal class ArticlesSearchViewModel : UsedeskViewModel() {

    val articlesLiveData = MutableLiveData<List<UsedeskArticleContent>>()
    private var lastQuery: String = ""

    fun onSearchQuery(searchQuery: String) {
        if (articlesLiveData.value == null || lastQuery != searchQuery) {
            lastQuery = searchQuery
            doIt(UsedeskKnowledgeBaseSdk.requireInstance().getArticlesRx(searchQuery), onValue = {
                articlesLiveData.postValue(it)
            })
        }
    }
}