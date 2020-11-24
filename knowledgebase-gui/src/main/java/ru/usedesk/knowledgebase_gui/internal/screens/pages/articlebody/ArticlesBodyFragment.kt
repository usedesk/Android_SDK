package ru.usedesk.knowledgebase_gui.internal.screens.pages.articlebody

import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.common_gui.external.UsedeskViewCustomizer
import ru.usedesk.knowledgebase_gui.internal.screens.common.ViewModelFactory
import ru.usedesk.knowledgebase_gui.internal.screens.pages.FragmentListView
import ru.usedesk.knowledgebase_sdk.external.IUsedeskKnowledgeBase
import ru.usedesk.knowledgebase_sdk.external.UsedeskKnowledgeBaseSdk.instance
import ru.usedesk.knowledgebase_sdk.external.entity.UsedeskArticleBody

class ArticlesBodyFragment : FragmentListView<UsedeskArticleBody?, ArticlesBodyViewModel?>() {
    private val usedeskKnowledgeBaseSdk: IUsedeskKnowledgeBase
    override fun getViewModelFactory(): ViewModelFactory<ArticlesBodyViewModel?>? {
        val searchQuery = nonNullArguments.getString(SEARCH_QUERY_KEY)
        return ArticlesBodyViewModel.Factory(usedeskKnowledgeBaseSdk, searchQuery!!)
    }

    protected fun getAdapter(list: List<UsedeskArticleBody>?): RecyclerView.Adapter<*> {
        if (parentFragment !is IOnArticleBodyClickListener) {
            throw RuntimeException("Parent fragment must implement " +
                    IOnArticleBodyClickListener::class.java.simpleName)
        }
        return ArticlesBodyAdapter(list!!, (parentFragment as IOnArticleBodyClickListener?)!!,
                UsedeskViewCustomizer.getInstance())
    }

    fun onSearchQueryUpdate(searchQuery: String) {
        viewModel.onSearchQueryUpdate(searchQuery)
    }

    companion object {
        const val SEARCH_QUERY_KEY = "searchQueryKey"
        fun newInstance(searchQuery: String): ArticlesBodyFragment {
            val args = Bundle()
            args.putString(SEARCH_QUERY_KEY, searchQuery)
            val articlesBodyFragment = ArticlesBodyFragment()
            articlesBodyFragment.arguments = args
            return articlesBodyFragment
        }
    }

    init {
        usedeskKnowledgeBaseSdk = instance
    }
}