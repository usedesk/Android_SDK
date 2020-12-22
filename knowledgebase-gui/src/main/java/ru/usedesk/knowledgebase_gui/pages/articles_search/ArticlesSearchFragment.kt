package ru.usedesk.knowledgebase_gui.pages.articles_search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.common_gui.UsedeskBinding
import ru.usedesk.common_gui.UsedeskFragment
import ru.usedesk.common_gui.inflateItem
import ru.usedesk.knowledgebase_gui.R

internal class ArticlesSearchFragment : UsedeskFragment() {

    private val viewModel: ArticlesSearchViewModel by viewModels()
    private lateinit var binding: Binding

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        doInit {
            binding = inflateItem(inflater,
                    container,
                    R.layout.usedesk_fragment_list,
                    R.style.Usedesk_KnowledgeBase) { rootView, defaultStyleId ->
                Binding(rootView, defaultStyleId)
            }

            val searchQuery = argsGetString(SEARCH_QUERY_KEY)
            if (searchQuery != null) {
                init(searchQuery)
            }
        }

        return binding.rootView
    }

    private fun init(searchQuery: String) {
        viewModel.onSearchQuery(searchQuery)

        ArticlesSearchAdapter(binding.rvItems,
                viewLifecycleOwner,
                viewModel) {
            getParentListener<IOnArticlesSearchClickListener>()?.onArticleClick(it)
        }
    }

    fun onSearchQueryUpdate(searchQuery: String) {
        viewModel.onSearchQuery(searchQuery)
    }

    companion object {
        private const val SEARCH_QUERY_KEY = "searchQueryKey"

        fun newInstance(searchQuery: String): ArticlesSearchFragment {
            return ArticlesSearchFragment().apply {
                arguments = Bundle().apply {
                    putString(SEARCH_QUERY_KEY, searchQuery)
                }
            }
        }
    }

    internal class Binding(rootView: View, defaultStyleId: Int) : UsedeskBinding(rootView, defaultStyleId) {
        val rvItems: RecyclerView = rootView.findViewById(R.id.rv_items)
    }
}