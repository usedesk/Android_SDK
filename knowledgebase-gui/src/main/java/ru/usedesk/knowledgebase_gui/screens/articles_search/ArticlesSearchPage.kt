package ru.usedesk.knowledgebase_gui.screens.articles_search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ru.usedesk.common_gui.UsedeskBinding
import ru.usedesk.common_gui.UsedeskFragment
import ru.usedesk.common_gui.inflateItem
import ru.usedesk.knowledgebase_gui.IUsedeskOnSupportClickListener
import ru.usedesk.knowledgebase_gui.R
import ru.usedesk.knowledgebase_gui.screens.main.IOnArticleClickListener

internal class ArticlesSearchPage : UsedeskFragment() {

    private val viewModel: ArticlesSearchViewModel by viewModels()
    private lateinit var binding: Binding

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = inflateItem(inflater,
                container,
                R.layout.usedesk_page_list,
                R.style.Usedesk_KnowledgeBase_Articles_Search_Page) { rootView, defaultStyleId ->
            Binding(rootView, defaultStyleId)
        }.apply {
            tvMessage.text = styleValues
                    .getStyleValues(R.attr.usedesk_knowledgebase_article_search_page_message)
                    .getString(R.attr.usedesk_text_1)

            btnSupport.setOnClickListener {
                requireParentFragment().also {
                    if (it is IUsedeskOnSupportClickListener) {
                        it.onSupportClick()
                    }
                }
            }
        }

        ArticlesSearchAdapter(binding.rvItems,
                viewLifecycleOwner,
                viewModel) { articleContent ->
            getParentListener<IOnArticleClickListener>()?.onArticleClick(
                    articleContent.categoryId,
                    articleContent.id,
                    articleContent.title)
        }

        viewModel.articlesLiveData.observe(viewLifecycleOwner) {
            when {
                it == null -> {
                    binding.pbLoading.visibility = View.VISIBLE
                    binding.tvMessage.visibility = View.GONE
                    binding.rvItems.visibility = View.GONE
                }
                it.isEmpty() -> {
                    binding.pbLoading.visibility = View.GONE
                    binding.tvMessage.visibility = View.VISIBLE
                    binding.rvItems.visibility = View.GONE
                }
                else -> {
                    binding.pbLoading.visibility = View.GONE
                    binding.tvMessage.visibility = View.GONE
                    binding.rvItems.visibility = View.VISIBLE
                }
            }
        }

        if (savedInstanceState == null) {
            viewModel.onSearchQuery("")
        }

        return binding.rootView
    }

    fun onSearchQueryUpdate(searchQuery: String) {
        viewModel.onSearchQuery(searchQuery)
    }

    companion object {
        fun newInstance(): ArticlesSearchPage {
            return ArticlesSearchPage()
        }
    }

    internal class Binding(rootView: View, defaultStyleId: Int) : UsedeskBinding(rootView, defaultStyleId) {
        val rvItems: RecyclerView = rootView.findViewById(R.id.rv_items)
        val pbLoading: ProgressBar = rootView.findViewById(R.id.pb_loading)
        val tvMessage: TextView = rootView.findViewById(R.id.tv_message)
        val btnSupport: FloatingActionButton = rootView.findViewById(R.id.fab_support)
    }
}