package ru.usedesk.knowledgebase_gui.internal.screens.pages.articlesinfo

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.common_gui.external.IUsedeskViewCustomizer
import ru.usedesk.knowledgebase_gui.R
import ru.usedesk.knowledgebase_sdk.external.entity.UsedeskArticleInfo
import java.util.*

class ArticlesInfoAdapter internal constructor(private val articleInfoList: List<UsedeskArticleInfo>,
                                               private val onArticleClickListener: IOnArticleInfoClickListener,
                                               private val usedeskViewCustomizer: IUsedeskViewCustomizer) : RecyclerView.Adapter<ArticlesInfoAdapter.ArticleViewHolder>() {
    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ArticleViewHolder {
        val view = usedeskViewCustomizer.createView(viewGroup, R.layout.usedesk_item_article_info, R.style.Usedesk_Theme_KnowledgeBase)
        return ArticleViewHolder(view)
    }

    override fun onBindViewHolder(articleViewHolder: ArticleViewHolder, i: Int) {
        articleViewHolder.bind(articleInfoList[i])
    }

    override fun getItemCount(): Int {
        return articleInfoList.size
    }

    internal inner class ArticleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewTitle: TextView
        private val textViewCount: TextView
        fun bind(articleInfo: UsedeskArticleInfo) {
            textViewTitle.text = articleInfo.title
            textViewCount.text = String.format(Locale.getDefault(), "%d", articleInfo.views)
            itemView.setOnClickListener { v: View? -> onArticleClickListener.onArticleInfoClick(articleInfo.id) }
        }

        init {
            textViewTitle = itemView.findViewById(R.id.tv_title)
            textViewCount = itemView.findViewById(R.id.tv_count)
        }
    }
}