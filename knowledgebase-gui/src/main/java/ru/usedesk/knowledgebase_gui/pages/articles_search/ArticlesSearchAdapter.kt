package ru.usedesk.knowledgebase_gui.pages.articles_search

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.common_gui.UsedeskBinding
import ru.usedesk.common_gui.inflateItem
import ru.usedesk.knowledgebase_gui.R
import ru.usedesk.knowledgebase_sdk.entity.UsedeskArticleBody

internal class ArticlesSearchAdapter(
        recyclerView: RecyclerView,
        lifecycleOwner: LifecycleOwner,
        viewModel: ViewModel,
        private val onArticleClick: (Long) -> Unit
) : RecyclerView.Adapter<ArticlesSearchAdapter.ArticleViewHolder>() {

    private var items = listOf<UsedeskArticleBody>()

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ArticleViewHolder {
        return ArticleViewHolder(inflateItem(viewGroup,
                R.layout.usedesk_item_article_body,
                R.style.Usedesk_KnowledgeBase) { rootView, defaultStyleId ->
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

        fun bind(articleBody: UsedeskArticleBody) {
            binding.tvTitle.text = articleBody.title
            binding.tvDescription.text = articleBody.text
            binding.rootView.setOnClickListener {
                onArticleClick(articleBody.id)
            }
        }
    }

    internal class Binding(rootView: View, defaultStyleId: Int) : UsedeskBinding(rootView, defaultStyleId) {
        val tvTitle: TextView = rootView.findViewById(R.id.tv_title)
        val tvDescription: TextView = rootView.findViewById(R.id.tv_description)
        val tvPath: TextView = rootView.findViewById(R.id.tv_path)
    }
}