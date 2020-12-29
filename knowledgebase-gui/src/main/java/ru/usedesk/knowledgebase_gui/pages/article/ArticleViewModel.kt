package ru.usedesk.knowledgebase_gui.pages.article

import android.annotation.SuppressLint
import ru.usedesk.knowledgebase_gui.common.DataViewModel
import ru.usedesk.knowledgebase_sdk.UsedeskKnowledgeBaseSdk
import ru.usedesk.knowledgebase_sdk.entity.UsedeskArticleBody

class ArticleViewModel : DataViewModel<UsedeskArticleBody>() {

    @SuppressLint("CheckResult")
    override fun onData(data: UsedeskArticleBody) {
        super.onData(data)
        UsedeskKnowledgeBaseSdk.getInstance().addViewsRx(data.id)
                .subscribe({}) { it.printStackTrace() }
    }

    fun init(articleId: Long) {
        loadData(UsedeskKnowledgeBaseSdk.getInstance().getArticleRx(articleId))
    }
}