package ru.usedesk.knowledgebase_gui.pages.articlesinfo

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.knowledgebase_gui.R
import ru.usedesk.knowledgebase_gui.entity.DataOrMessage
import ru.usedesk.knowledgebase_gui.pages.FragmentListView
import ru.usedesk.knowledgebase_sdk.entity.UsedeskArticleInfo

class ArticlesInfoFragment : FragmentListView<UsedeskArticleInfo>(
        R.layout.usedesk_fragment_list,
        R.style.Usedesk_Theme_KnowledgeBase
) {

    private val viewModel: ArticlesInfoViewModel by viewModels()

    override fun getAdapter(list: List<UsedeskArticleInfo>): RecyclerView.Adapter<*> {
        if (parentFragment !is IOnArticleInfoClickListener) {
            throw RuntimeException("Parent fragment must implement " +
                    IOnArticleInfoClickListener::class.java.simpleName)
        }
        return ArticlesInfoAdapter(list, parentFragment as IOnArticleInfoClickListener)
    }

    override fun init() {
        val categoryId = argsGetLong(CATEGORY_ID_KEY)
        if (categoryId != null) {
            viewModel.init(categoryId)
        }
    }

    override fun getLiveData(): LiveData<DataOrMessage<List<UsedeskArticleInfo>>> = viewModel.liveData

    companion object {
        private const val CATEGORY_ID_KEY = "categoryIdKey"

        fun newInstance(categoryId: Long): ArticlesInfoFragment {
            return ArticlesInfoFragment().apply {
                arguments = Bundle().apply {
                    putLong(CATEGORY_ID_KEY, categoryId)
                }
            }
        }
    }
}