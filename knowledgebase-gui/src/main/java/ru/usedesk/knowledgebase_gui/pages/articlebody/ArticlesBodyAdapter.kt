package ru.usedesk.knowledgebase_gui.pages.articlebody

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.common_gui.inflateItem
import ru.usedesk.knowledgebase_gui.R
import ru.usedesk.knowledgebase_gui.databinding.UsedeskItemArticleInfoBinding
import ru.usedesk.knowledgebase_sdk.entity.UsedeskArticleBody
import java.util.*

class ArticlesBodyAdapter(
        private val articleInfoList: List<UsedeskArticleBody>,
        private val onArticleClickListener: IOnArticleBodyClickListener
) : RecyclerView.Adapter<ArticlesBodyAdapter.ArticleViewHolder>() {

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

        fun bind(articleBody: UsedeskArticleBody) {
            binding.tvTitle.text = articleBody.title
            binding.tvCount.text = String.format(Locale.getDefault(), "%d", articleBody.views)
            itemView.setOnClickListener {
                onArticleClickListener.onArticleBodyClick(articleBody.id)
            }
        }
    }
}