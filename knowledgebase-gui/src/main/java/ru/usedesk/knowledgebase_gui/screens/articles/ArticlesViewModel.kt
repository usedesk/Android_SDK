package ru.usedesk.knowledgebase_gui.screens.articles

import androidx.lifecycle.MutableLiveData
import ru.usedesk.common_gui.UsedeskViewModel
import ru.usedesk.knowledgebase_sdk.UsedeskKnowledgeBaseSdk
import ru.usedesk.knowledgebase_sdk.entity.UsedeskArticleInfo

internal class ArticlesViewModel : UsedeskViewModel() {

    val articleInfoListLiveData = MutableLiveData<List<UsedeskArticleInfo>>()

    fun init(categoryId: Long) {
        doIt(UsedeskKnowledgeBaseSdk.getInstance()
                .getArticlesRx(categoryId), onValue = {
            articleInfoListLiveData.postValue(it)
        })
    }
}