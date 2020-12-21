package ru.usedesk.knowledgebase_gui.pages.articlesinfo

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.common_gui.UsedeskBinding
import ru.usedesk.common_gui.inflateItem
import ru.usedesk.knowledgebase_gui.R
import ru.usedesk.knowledgebase_sdk.entity.UsedeskArticleInfo

internal class ArticlesInfoAdapter(
        recyclerView: RecyclerView,
        lifecycleOwner: LifecycleOwner,
        private val viewModel: ArticlesInfoViewModel,
        private val onArticleInfoClick: (Long) -> Unit
) : RecyclerView.Adapter<ArticlesInfoAdapter.ArticleViewHolder>() {

    private var items = listOf<UsedeskArticleInfo>()

    init {
        recyclerView.adapter = this

        viewModel.articleInfoListLiveData.observe(lifecycleOwner) {
            items = it
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ArticleViewHolder {
        return ArticleViewHolder(inflateItem(viewGroup,
                R.layout.usedesk_item_article_info,
                R.style.Usedesk_KnowledgeBase) { rootView, defaultStyleId ->
            ArticleInfoBinding(rootView, defaultStyleId)
        })
    }

    override fun onBindViewHolder(articleViewHolder: ArticleViewHolder, i: Int) {
        articleViewHolder.bind(items[i])
    }

    override fun getItemCount() = items.size

    inner class ArticleViewHolder(
            private val binding: ArticleInfoBinding
    ) : RecyclerView.ViewHolder(binding.rootView) {

        fun bind(articleInfo: UsedeskArticleInfo) {
            binding.tvTitle.text = articleInfo.title
            binding.rootView.setOnClickListener {
                onArticleInfoClick(articleInfo.id)
            }
        }
    }

    internal class ArticleInfoBinding(rootView: View, defaultStyleId: Int) : UsedeskBinding(rootView, defaultStyleId) {
        val tvTitle: TextView = rootView.findViewById(R.id.tv_title)
    }
}