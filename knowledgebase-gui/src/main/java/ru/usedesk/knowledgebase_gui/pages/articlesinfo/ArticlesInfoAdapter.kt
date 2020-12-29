package ru.usedesk.knowledgebase_gui.pages.articlesinfo

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.common_gui.UsedeskBinding
import ru.usedesk.common_gui.inflateItem
import ru.usedesk.knowledgebase_gui.R
import ru.usedesk.knowledgebase_sdk.entity.UsedeskArticleInfo
import java.util.*

internal class ArticlesInfoAdapter(
        private val articleInfoList: List<UsedeskArticleInfo>,
        private val onArticleClickListener: IOnArticleInfoClickListener
) : RecyclerView.Adapter<ArticlesInfoAdapter.ArticleViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ArticleViewHolder {
        return ArticleViewHolder(inflateItem(viewGroup,
                R.layout.usedesk_item_article_info,
                R.style.Usedesk_KnowledgeBase) { rootView, defaultStyleId ->
            ArticleInfoBinding(rootView, defaultStyleId)
        })
    }

    override fun onBindViewHolder(articleViewHolder: ArticleViewHolder, i: Int) {
        articleViewHolder.bind(articleInfoList[i])
    }

    override fun getItemCount() = articleInfoList.size

    inner class ArticleViewHolder(
            private val binding: ArticleInfoBinding
    ) : RecyclerView.ViewHolder(binding.rootView) {

        fun bind(articleInfo: UsedeskArticleInfo) {
            binding.tvTitle.text = articleInfo.title
            binding.tvCount.text = String.format(Locale.getDefault(), "%d", articleInfo.views)
            itemView.setOnClickListener {
                onArticleClickListener.onArticleInfoClick(articleInfo.id)
            }
        }
    }

    internal class ArticleInfoBinding(rootView: View, defaultStyleId: Int) : UsedeskBinding(rootView, defaultStyleId) {
        val tvTitle: TextView = rootView.findViewById(R.id.tv_title)
        val tvCount: TextView = rootView.findViewById(R.id.tv_count)
    }
}