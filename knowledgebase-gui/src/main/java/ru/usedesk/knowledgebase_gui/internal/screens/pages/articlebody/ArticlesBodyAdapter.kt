package ru.usedesk.knowledgebase_gui.internal.screens.pages.articlebody

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.common_gui.internal.inflateItem
import ru.usedesk.knowledgebase_gui.R
import ru.usedesk.knowledgebase_sdk.external.entity.UsedeskArticleBody
import java.util.*

class ArticlesBodyAdapter(
        private val articleInfoList: List<UsedeskArticleBody>,
        private val onArticleClickListener: IOnArticleBodyClickListener
) : RecyclerView.Adapter<ArticlesBodyAdapter.ArticleViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ArticleViewHolder {
        val view: View = inflateItem(R.layout.usedesk_item_article_info, viewGroup)
        return ArticleViewHolder(view)
    }

    override fun onBindViewHolder(articleViewHolder: ArticleViewHolder, i: Int) {
        articleViewHolder.bind(articleInfoList[i])
    }

    override fun getItemCount(): Int {
        return articleInfoList.size
    }

    inner class ArticleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewTitle: TextView = itemView.findViewById(R.id.tv_title)
        private val textViewCount: TextView = itemView.findViewById(R.id.tv_count)

        fun bind(articleBody: UsedeskArticleBody) {
            textViewTitle.text = articleBody.title
            textViewCount.text = String.format(Locale.getDefault(), "%d", articleBody.views)
            itemView.setOnClickListener {
                onArticleClickListener.onArticleBodyClick(articleBody.id)
            }
        }

    }
}