package ru.usedesk.knowledgebase_gui.screens.articles

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.common_gui.IUsedeskAdapter
import ru.usedesk.common_gui.UsedeskBinding
import ru.usedesk.common_gui.inflateItem
import ru.usedesk.knowledgebase_gui.R
import ru.usedesk.knowledgebase_sdk.entity.UsedeskArticleInfo

internal class ArticlesAdapter(
        recyclerView: RecyclerView,
        private val onArticleInfoClick: (UsedeskArticleInfo) -> Unit
) : RecyclerView.Adapter<ArticlesAdapter.ArticleViewHolder>(), IUsedeskAdapter<ArticlesViewModel> {

    private var items = listOf<UsedeskArticleInfo>()

    init {
        recyclerView.adapter = this
    }

    override fun onLiveData(viewModel: ArticlesViewModel, lifecycleOwner: LifecycleOwner) {
        viewModel.articleInfoListLiveData.observe(lifecycleOwner) {
            (it ?: listOf()).apply {
                if (items != this) {
                    items = this
                    notifyDataSetChanged()
                }
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ArticleViewHolder {
        return ArticleViewHolder(inflateItem(viewGroup,
                R.layout.usedesk_item_article_info,
                R.style.Usedesk_KnowledgeBase_Articles_Page_Article) { rootView, defaultStyleId ->
            Binding(rootView, defaultStyleId)
        })
    }

    override fun onBindViewHolder(articleViewHolder: ArticleViewHolder, i: Int) {
        articleViewHolder.bind(items[i])
    }

    override fun getItemCount() = items.size

    inner class ArticleViewHolder(
            private val binding: Binding
    ) : RecyclerView.ViewHolder(binding.rootView) {

        fun bind(articleInfo: UsedeskArticleInfo) {
            binding.tvTitle.text = articleInfo.title
            binding.rootView.setOnClickListener {
                onArticleInfoClick(articleInfo)
            }
        }
    }

    internal class Binding(rootView: View, defaultStyleId: Int) : UsedeskBinding(rootView, defaultStyleId) {
        val tvTitle: TextView = rootView.findViewById(R.id.tv_title)
    }
}