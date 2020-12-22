package ru.usedesk.knowledgebase_gui.pages.articles_search

import androidx.lifecycle.MutableLiveData
import ru.usedesk.common_gui.UsedeskViewModel
import ru.usedesk.knowledgebase_sdk.UsedeskKnowledgeBaseSdk
import ru.usedesk.knowledgebase_sdk.entity.UsedeskArticleBody

internal class ArticlesSearchViewModel : UsedeskViewModel() {

    val articlesLiveData = MutableLiveData<List<UsedeskArticleBody>>()

    fun onSearchQuery(searchQuery: String) {
        doIt(UsedeskKnowledgeBaseSdk.getInstance().getArticlesRx(searchQuery), onValue = {
            articlesLiveData.postValue(it)
        })
    }
}