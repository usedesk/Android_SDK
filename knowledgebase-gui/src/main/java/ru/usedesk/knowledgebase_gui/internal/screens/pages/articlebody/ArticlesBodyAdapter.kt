package ru.usedesk.knowledgebase_gui.internal.screens.pages.articlebody

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.common_gui.external.IUsedeskViewCustomizer
import ru.usedesk.knowledgebase_gui.R
import ru.usedesk.knowledgebase_sdk.external.entity.UsedeskArticleBody
import java.util.*

class ArticlesBodyAdapter internal constructor(private val articleInfoList: List<UsedeskArticleBody>,
                                               private val onArticleClickListener: IOnArticleBodyClickListener,
                                               private val usedeskViewCustomizer: IUsedeskViewCustomizer) : RecyclerView.Adapter<ArticlesBodyAdapter.ArticleViewHolder>() {
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
        fun bind(articleBody: UsedeskArticleBody) {
            textViewTitle.text = articleBody.title
            textViewCount.text = String.format(Locale.getDefault(), "%d", articleBody.views)
            itemView.setOnClickListener { v: View? -> onArticleClickListener.onArticleBodyClick(articleBody.id) }
        }

        init {
            textViewTitle = itemView.findViewById(R.id.tv_title)
            textViewCount = itemView.findViewById(R.id.tv_count)
        }
    }
}