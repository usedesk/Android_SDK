package ru.usedesk.knowledgebase_gui.screens.articles

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.common_gui.UsedeskBinding
import ru.usedesk.common_gui.inflateItem
import ru.usedesk.common_gui.onEachWithOld
import ru.usedesk.knowledgebase_gui.R
import ru.usedesk.knowledgebase_sdk.entity.UsedeskArticleInfo

internal class ArticlesAdapter(
    recyclerView: RecyclerView,
    viewModel: ArticlesViewModel,
    lifecycleCoroutineScope: LifecycleCoroutineScope,
    private val onArticleInfoClick: (UsedeskArticleInfo) -> Unit
) : RecyclerView.Adapter<ArticlesAdapter.ArticleViewHolder>() {

    private var items = listOf<UsedeskArticleInfo>()

    init {
        recyclerView.adapter = this
        viewModel.modelFlow.onEachWithOld(lifecycleCoroutineScope) { old, new ->
            if (old?.articleInfoList != new.articleInfoList) {
                val oldItems = items
                val newItems = new.articleInfoList
                items = newItems

                DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                    override fun getOldListSize() = oldItems.size

                    override fun getNewListSize() = newItems.size

                    override fun areItemsTheSame(
                        oldItemPosition: Int,
                        newItemPosition: Int
                    ): Boolean {
                        val oldItem = oldItems[oldItemPosition]
                        val newItem = newItems[newItemPosition]
                        return oldItem.id == newItem.id
                    }

                    override fun areContentsTheSame(
                        oldItemPosition: Int,
                        newItemPosition: Int
                    ): Boolean {
                        val oldItem = oldItems[oldItemPosition]
                        val newItem = newItems[newItemPosition]
                        return oldItem.categoryId == newItem.categoryId &&
                                oldItem.title == newItem.title
                    }
                }).dispatchUpdatesTo(this)
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ArticleViewHolder {
        return ArticleViewHolder(inflateItem(
            viewGroup,
            R.layout.usedesk_item_article_info,
            R.style.Usedesk_KnowledgeBase_Articles_Page_Article
        ) { rootView, defaultStyleId ->
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
            binding.lClickable.setOnClickListener {
                onArticleInfoClick(articleInfo)
            }
        }
    }

    internal class Binding(rootView: View, defaultStyleId: Int) :
        UsedeskBinding(rootView, defaultStyleId) {
        val lClickable: ViewGroup = rootView.findViewById(R.id.l_clickable)
        val tvTitle: TextView = rootView.findViewById(R.id.tv_title)
    }
}