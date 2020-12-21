package ru.usedesk.knowledgebase_gui.pages.articlesinfo

import androidx.lifecycle.MutableLiveData
import ru.usedesk.common_gui.UsedeskViewModel
import ru.usedesk.knowledgebase_sdk.UsedeskKnowledgeBaseSdk
import ru.usedesk.knowledgebase_sdk.entity.UsedeskArticleInfo

internal class ArticlesInfoViewModel : UsedeskViewModel() {

    val articleInfoListLiveData = MutableLiveData<List<UsedeskArticleInfo>>()

    fun init(categoryId: Long) {
        doIt(UsedeskKnowledgeBaseSdk.getInstance()
                .getArticlesRx(categoryId), onValue = {
            articleInfoListLiveData.postValue(it)
        })
    }
}