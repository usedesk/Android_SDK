package ru.usedesk.knowledgebase_gui.internal.screens.pages.articlesinfo

import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.common_gui.external.UsedeskViewCustomizer
import ru.usedesk.knowledgebase_gui.internal.screens.common.ViewModelFactory
import ru.usedesk.knowledgebase_gui.internal.screens.pages.FragmentListView
import ru.usedesk.knowledgebase_sdk.external.IUsedeskKnowledgeBase
import ru.usedesk.knowledgebase_sdk.external.UsedeskKnowledgeBaseSdk.instance
import ru.usedesk.knowledgebase_sdk.external.entity.UsedeskArticleInfo

class ArticlesInfoFragment : FragmentListView<UsedeskArticleInfo?, ArticlesInfoViewModel?>() {
    private val usedeskKnowledgeBaseSdk: IUsedeskKnowledgeBase
    override fun getViewModelFactory(): ViewModelFactory<ArticlesInfoViewModel?>? {
        val categoryId = nonNullArguments.getLong(CATEGORY_ID_KEY)
        return ArticlesInfoViewModel.Factory(usedeskKnowledgeBaseSdk, categoryId)
    }

    protected fun getAdapter(list: List<UsedeskArticleInfo>?): RecyclerView.Adapter<*> {
        if (parentFragment !is IOnArticleInfoClickListener) {
            throw RuntimeException("Parent fragment must implement " +
                    IOnArticleInfoClickListener::class.java.simpleName)
        }
        return ArticlesInfoAdapter(list!!, (parentFragment as IOnArticleInfoClickListener?)!!,
                UsedeskViewCustomizer.getInstance())
    }

    companion object {
        const val CATEGORY_ID_KEY = "categoryIdKey"
        fun newInstance(categoryId: Long): ArticlesInfoFragment {
            val args = Bundle()
            args.putLong(CATEGORY_ID_KEY, categoryId)
            val articlesInfoFragment = ArticlesInfoFragment()
            articlesInfoFragment.arguments = args
            return articlesInfoFragment
        }
    }

    init {
        usedeskKnowledgeBaseSdk = instance
    }
}