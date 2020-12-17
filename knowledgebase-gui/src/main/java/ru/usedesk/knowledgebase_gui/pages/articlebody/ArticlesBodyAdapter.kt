package ru.usedesk.knowledgebase_gui.pages.articlebody

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.common_gui.UsedeskBinding
import ru.usedesk.common_gui.inflateItem
import ru.usedesk.knowledgebase_gui.R
import ru.usedesk.knowledgebase_sdk.entity.UsedeskArticleBody
import java.util.*

internal class ArticlesBodyAdapter(
        private val articleInfoList: List<UsedeskArticleBody>,
        private val onArticleClickListener: IOnArticleBodyClickListener
) : RecyclerView.Adapter<ArticlesBodyAdapter.ArticleViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ArticleViewHolder {
        return ArticleViewHolder(inflateItem(viewGroup,
                R.layout.usedesk_item_article_info,
                R.style.Usedesk_KnowledgeBase) {
            ArticleBodyBinding(it)
        })
    }

    override fun onBindViewHolder(articleViewHolder: ArticleViewHolder, i: Int) {
        articleViewHolder.bind(articleInfoList[i])
    }

    override fun getItemCount() = articleInfoList.size

    inner class ArticleViewHolder(
            private val binding: ArticleBodyBinding
    ) : RecyclerView.ViewHolder(binding.rootView) {

        fun bind(articleBody: UsedeskArticleBody) {
            binding.tvTitle.text = articleBody.title
            binding.tvCount.text = String.format(Locale.getDefault(), "%d", articleBody.views)
            itemView.setOnClickListener {
                onArticleClickListener.onArticleBodyClick(articleBody.id)
            }
        }
    }

    internal class ArticleBodyBinding(rootView: View) : UsedeskBinding(rootView) {
        val tvTitle: TextView = rootView.findViewById(R.id.tv_title)
        val tvCount: TextView = rootView.findViewById(R.id.tv_count)
    }
}