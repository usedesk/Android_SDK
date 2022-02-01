package ru.usedesk.knowledgebase_gui.screens.articles_search

import android.text.Html
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.common_gui.UsedeskBinding
import ru.usedesk.common_gui.inflateItem
import ru.usedesk.knowledgebase_gui.R
import ru.usedesk.knowledgebase_sdk.entity.UsedeskArticleContent

internal class ArticlesSearchAdapter(
    recyclerView: RecyclerView,
    viewModel: ArticlesSearchViewModel,
    lifecycleOwner: LifecycleOwner,
    private val onArticleClick: (UsedeskArticleContent) -> Unit
) : RecyclerView.Adapter<ArticlesSearchAdapter.ArticleViewHolder>() {

    private var articles = listOf<UsedeskArticleContent>()

    init {
        recyclerView.adapter = this
        viewModel.modelLiveData.initAndObserveWithOld(lifecycleOwner) { old, new ->
            if (old?.articles != new.articles) {
                val oldItems = articles
                val newItems = new.articles
                articles = new.articles

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
                        return oldItem.title == newItem.title &&
                                oldItem.text == newItem.text
                    }
                }).dispatchUpdatesTo(this)
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ArticleViewHolder {
        return ArticleViewHolder(inflateItem(
            viewGroup,
            R.layout.usedesk_item_article_content,
            R.style.Usedesk_KnowledgeBase_Articles_Search_Page_Article
        ) { rootView, defaultStyleId ->
            Binding(rootView, defaultStyleId)
        })
    }

    override fun onBindViewHolder(articleViewHolder: ArticleViewHolder, i: Int) {
        articleViewHolder.bind(articles[i])
    }

    override fun getItemCount() = articles.size

    inner class ArticleViewHolder(
        private val binding: Binding
    ) : RecyclerView.ViewHolder(binding.rootView) {

        fun bind(articleContent: UsedeskArticleContent) {
            binding.tvTitle.text = articleContent.title
            binding.tvDescription.text = Html.fromHtml(articleContent.text).trim()
            binding.lClickable.setOnClickListener {
                onArticleClick(articleContent)
            }
        }
    }

    internal class Binding(rootView: View, defaultStyleId: Int) :
        UsedeskBinding(rootView, defaultStyleId) {
        val lClickable: ViewGroup = rootView.findViewById(R.id.l_clickable)
        val tvTitle: TextView = rootView.findViewById(R.id.tv_title)
        val tvDescription: TextView = rootView.findViewById(R.id.tv_description)
        val tvPath: TextView = rootView.findViewById(R.id.tv_path)
    }
}