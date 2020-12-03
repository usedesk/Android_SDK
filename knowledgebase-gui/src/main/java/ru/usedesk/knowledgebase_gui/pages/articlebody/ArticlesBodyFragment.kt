package ru.usedesk.knowledgebase_gui.pages.articlebody

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.knowledgebase_gui.R
import ru.usedesk.knowledgebase_gui.entity.DataOrMessage
import ru.usedesk.knowledgebase_gui.pages.FragmentListView
import ru.usedesk.knowledgebase_sdk.entity.UsedeskArticleBody

class ArticlesBodyFragment : FragmentListView<UsedeskArticleBody>(
        R.layout.usedesk_fragment_list,
        R.style.Usedesk_Theme_KnowledgeBase
) {

    private val viewModel: ArticlesBodyViewModel by viewModels()

    override fun getAdapter(list: List<UsedeskArticleBody>): RecyclerView.Adapter<*> {
        if (parentFragment !is IOnArticleBodyClickListener) {
            throw RuntimeException("Parent fragment must implement " +
                    IOnArticleBodyClickListener::class.java.simpleName)
        }
        return ArticlesBodyAdapter(list, parentFragment as IOnArticleBodyClickListener)
    }

    override fun init() {
        val searchQuery = argsGetString(SEARCH_QUERY_KEY)
        if (searchQuery != null) {
            viewModel.init(searchQuery)
        }
    }

    override fun getLiveData(): LiveData<DataOrMessage<List<UsedeskArticleBody>>> = viewModel.liveData

    fun onSearchQueryUpdate(searchQuery: String) {
        viewModel.onSearchQueryUpdate(searchQuery)
    }

    companion object {
        private const val SEARCH_QUERY_KEY = "searchQueryKey"

        fun newInstance(searchQuery: String): ArticlesBodyFragment {
            return ArticlesBodyFragment().apply {
                arguments = Bundle().apply {
                    putString(SEARCH_QUERY_KEY, searchQuery)
                }
            }
        }
    }
}