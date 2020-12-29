package ru.usedesk.knowledgebase_gui.screens.articles_search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.common_gui.UsedeskBinding
import ru.usedesk.common_gui.UsedeskFragment
import ru.usedesk.common_gui.inflateItem
import ru.usedesk.knowledgebase_gui.R

internal class ArticlesSearchPage : UsedeskFragment() {

    private val viewModel: ArticlesSearchViewModel by viewModels()
    private lateinit var binding: Binding

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        doInit {
            binding = inflateItem(inflater,
                    container,
                    R.layout.usedesk_page_list,
                    R.style.Usedesk_KnowledgeBase) { rootView, defaultStyleId ->
                Binding(rootView, defaultStyleId)
            }

            init()
        }

        return binding.rootView
    }

    private fun init() {
        viewModel.onSearchQuery("")

        ArticlesSearchAdapter(binding.rvItems,
                viewLifecycleOwner,
                viewModel) {
            getParentListener<IOnArticlesSearchClickListener>()?.onArticleClick(it)
        }

        binding.tvMessage.setText(R.string.articles_not_found)
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
    }
}