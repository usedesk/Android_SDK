package ru.usedesk.knowledgebase_gui.screens.article

import androidx.lifecycle.MutableLiveData
import ru.usedesk.common_gui.UsedeskViewModel
import ru.usedesk.knowledgebase_sdk.UsedeskKnowledgeBaseSdk
import ru.usedesk.knowledgebase_sdk.entity.UsedeskArticleInfo

internal class ArticlePageViewModel : UsedeskViewModel() {

    val articlesLiveData = MutableLiveData<List<UsedeskArticleInfo>>()

    fun init(categoryId: Long) {
        doInit {
            doIt(UsedeskKnowledgeBaseSdk.getInstance()
                    .getArticlesRx(categoryId), onValue = {
                articlesLiveData.postValue(it)
            })
        }
    }
}