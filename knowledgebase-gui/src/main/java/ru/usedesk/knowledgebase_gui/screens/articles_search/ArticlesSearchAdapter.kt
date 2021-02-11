package ru.usedesk.knowledgebase_gui.screens.articles_search

import android.text.Html
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.common_gui.IUsedeskAdapter
import ru.usedesk.common_gui.UsedeskBinding
import ru.usedesk.common_gui.inflateItem
import ru.usedesk.knowledgebase_gui.R
import ru.usedesk.knowledgebase_sdk.entity.UsedeskArticleContent

internal class ArticlesSearchAdapter(
        recyclerView: RecyclerView,
        private val onArticleClick: (UsedeskArticleContent) -> Unit
) : RecyclerView.Adapter<ArticlesSearchAdapter.ArticleViewHolder>(), IUsedeskAdapter<ArticlesSearchViewModel> {

    private var articles = listOf<UsedeskArticleContent>()

    init {
        recyclerView.adapter = this
    }

    override fun onLiveData(viewModel: ArticlesSearchViewModel, lifecycleOwner: LifecycleOwner) {
        viewModel.articlesLiveData.observe(lifecycleOwner) {
            (it ?: listOf()).apply {
                if (articles != this) {
                    articles = this
                    notifyDataSetChanged()
                }
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ArticleViewHolder {
        return ArticleViewHolder(inflateItem(viewGroup,
                R.layout.usedesk_item_article_content,
                R.style.Usedesk_KnowledgeBase_Articles_Search_Page_Article) { rootView, defaultStyleId ->
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
            binding.rootView.setOnClickListener {
                onArticleClick(articleContent)
            }
        }
    }

    internal class Binding(rootView: View, defaultStyleId: Int) : UsedeskBinding(rootView, defaultStyleId) {
        val tvTitle: TextView = rootView.findViewById(R.id.tv_title)
        val tvDescription: TextView = rootView.findViewById(R.id.tv_description)
        val tvPath: TextView = rootView.findViewById(R.id.tv_path)
    }
}