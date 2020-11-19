package ru.usedesk.knowledgebase_gui.internal.screens.pages.articlebody

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.common_gui.internal.argsGetString
import ru.usedesk.knowledgebase_gui.R
import ru.usedesk.knowledgebase_gui.internal.screens.entity.DataOrMessage
import ru.usedesk.knowledgebase_gui.internal.screens.pages.FragmentListView
import ru.usedesk.knowledgebase_sdk.external.entity.UsedeskArticleBody

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
        val searchQuery = argsGetString(arguments, SEARCH_QUERY_KEY)
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

        @JvmOverloads
        fun newInstance(themeId: Int? = null, searchQuery: String): ArticlesBodyFragment {
            return ArticlesBodyFragment().apply {
                arguments = Bundle().apply {
                    if (themeId != null) {
                        putInt(THEME_ID_KEY, themeId)
                    }
                    putString(SEARCH_QUERY_KEY, searchQuery)
                }
            }
        }
    }
}