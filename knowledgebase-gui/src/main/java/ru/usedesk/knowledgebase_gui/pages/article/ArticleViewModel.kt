package ru.usedesk.knowledgebase_gui.pages.article

import androidx.lifecycle.MutableLiveData
import ru.usedesk.common_gui.UsedeskViewModel
import ru.usedesk.knowledgebase_sdk.UsedeskKnowledgeBaseSdk
import ru.usedesk.knowledgebase_sdk.entity.UsedeskArticleBody

internal class ArticleViewModel : UsedeskViewModel() {

    val articleLiveData = MutableLiveData<UsedeskArticleBody>()

    fun init(articleId: Long) {
        doIt(UsedeskKnowledgeBaseSdk.getInstance().getArticleRx(articleId), onValue = {
            articleLiveData.postValue(it)
            doIt(UsedeskKnowledgeBaseSdk.getInstance().addViewsRx(it.id))
        })
    }
}