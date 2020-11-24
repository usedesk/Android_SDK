package ru.usedesk.knowledgebase_gui.internal.screens.pages.article

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.widget.TextView
import ru.usedesk.knowledgebase_gui.R
import ru.usedesk.knowledgebase_gui.internal.screens.common.FragmentDataView
import ru.usedesk.knowledgebase_gui.internal.screens.common.ViewModelFactory
import ru.usedesk.knowledgebase_sdk.external.IUsedeskKnowledgeBase
import ru.usedesk.knowledgebase_sdk.external.UsedeskKnowledgeBaseSdk.instance
import ru.usedesk.knowledgebase_sdk.external.entity.UsedeskArticleBody

class ArticleFragment : FragmentDataView<UsedeskArticleBody?, ArticleViewModel?>(R.layout.usedesk_fragment_article) {
    private val usedeskKnowledgeBaseSdk: IUsedeskKnowledgeBase
    private var textViewTitle: TextView? = null
    private var contentWebView: WebView? = null
    override fun onView(view: View) {
        super.onView(view)
        textViewTitle = view.findViewById(R.id.tv_title)
        contentWebView = view.findViewById(R.id.wv_content)
        contentWebView.setBackgroundColor(Color.TRANSPARENT)
    }

    override fun getViewModelFactory(): ViewModelFactory<ArticleViewModel?>? {
        val articleId = nonNullArguments.getLong(ARTICLE_ID_KEY)
        return ArticleViewModel.Factory(usedeskKnowledgeBaseSdk, articleId)
    }

    protected override fun setDataView(data: UsedeskArticleBody) {
        textViewTitle!!.text = data.title
        contentWebView!!.loadData(data.text, "text/html", null)
    }

    companion object {
        private const val ARTICLE_ID_KEY = "articleIdKey"
        fun newInstance(articleId: Long): ArticleFragment {
            val args = Bundle()
            args.putLong(ARTICLE_ID_KEY, articleId)
            val articleFragment = ArticleFragment()
            articleFragment.arguments = args
            return articleFragment
        }
    }

    init {
        usedeskKnowledgeBaseSdk = instance
    }
}