package ru.usedesk.knowledgebase_gui.screens.article

import androidx.lifecycle.MutableLiveData
import ru.usedesk.common_gui.UsedeskViewModel
import ru.usedesk.knowledgebase_sdk.UsedeskKnowledgeBaseSdk
import ru.usedesk.knowledgebase_sdk.entity.UsedeskArticleInfo

internal class ArticlePageViewModel : UsedeskViewModel() {

    val articlesLiveData = MutableLiveData<List<UsedeskArticleInfo>>()
    val selectedPositionLiveData = MutableLiveData(0)
    val selectedArticleLiveData = MutableLiveData<UsedeskArticleInfo>()

    fun init(categoryId: Long,
             articleId: Long) {
        doInit {
            doIt(UsedeskKnowledgeBaseSdk.getInstance()
                    .getArticlesRx(categoryId), onValue = { articles ->
                articlesLiveData.postValue(articles)

                val position = articles.indexOfFirst {
                    it.id == articleId
                }
                if (position >= 0) {
                    selectedArticleLiveData.postValue(articles[position])
                    selectedPositionLiveData.postValue(position)
                }
            })
        }
    }

    fun onSelect(position: Int) {
        selectedPositionLiveData.value = position
        selectedArticleLiveData.value = articlesLiveData.value?.getOrNull(position)
    }
}