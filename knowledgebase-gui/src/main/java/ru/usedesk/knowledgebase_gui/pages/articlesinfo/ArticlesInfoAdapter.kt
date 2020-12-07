package ru.usedesk.knowledgebase_gui.pages.articlesinfo

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.common_gui.inflateItem
import ru.usedesk.knowledgebase_gui.R
import ru.usedesk.knowledgebase_gui.databinding.UsedeskItemArticleInfoBinding
import ru.usedesk.knowledgebase_sdk.entity.UsedeskArticleInfo
import java.util.*

class ArticlesInfoAdapter(
        private val articleInfoList: List<UsedeskArticleInfo>,
        private val onArticleClickListener: IOnArticleInfoClickListener
) : RecyclerView.Adapter<ArticlesInfoAdapter.ArticleViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ArticleViewHolder {
        return ArticleViewHolder(inflateItem(viewGroup,
                R.layout.usedesk_item_article_info,
                R.style.Usedesk_Theme_KnowledgeBase))
    }

    override fun onBindViewHolder(articleViewHolder: ArticleViewHolder, i: Int) {
        articleViewHolder.bind(articleInfoList[i])
    }

    override fun getItemCount() = articleInfoList.size

    inner class ArticleViewHolder(
            private val binding: UsedeskItemArticleInfoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(articleInfo: UsedeskArticleInfo) {
            binding.tvTitle.text = articleInfo.title
            binding.tvCount.text = String.format(Locale.getDefault(), "%d", articleInfo.views)
            itemView.setOnClickListener {
                onArticleClickListener.onArticleInfoClick(articleInfo.id)
            }
        }
    }
}