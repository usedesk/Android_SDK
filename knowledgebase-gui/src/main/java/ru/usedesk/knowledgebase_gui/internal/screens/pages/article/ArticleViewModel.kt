package ru.usedesk.knowledgebase_gui.internal.screens.pages.article

import ru.usedesk.knowledgebase_gui.internal.screens.common.DataViewModel
import ru.usedesk.knowledgebase_gui.internal.screens.common.ViewModelFactory
import ru.usedesk.knowledgebase_sdk.external.IUsedeskKnowledgeBase
import ru.usedesk.knowledgebase_sdk.external.entity.UsedeskArticleBody

class ArticleViewModel private constructor(private val usedeskKnowledgeBaseSdk: IUsedeskKnowledgeBase, articleId: Long) : DataViewModel<UsedeskArticleBody?>() {
    protected override fun onData(data: UsedeskArticleBody) {
        super.onData(data)
        val ignored = usedeskKnowledgeBaseSdk.addViewsRx(data.id)
                .subscribe({}) { obj: Throwable -> obj.printStackTrace() }
    }

    internal class Factory(private val iUsedeskKnowledgeBase: IUsedeskKnowledgeBase, private val articleId: Long) : ViewModelFactory<ArticleViewModel?>() {
        override fun create(): ArticleViewModel {
            return ArticleViewModel(iUsedeskKnowledgeBase, articleId)
        }

        override fun getClassType(): Class<ArticleViewModel?> {
            return ArticleViewModel::class.java
        }
    }

    init {
        loadData(usedeskKnowledgeBaseSdk.getArticleRx(articleId))
    }
}