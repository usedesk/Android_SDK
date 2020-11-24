package ru.usedesk.knowledgebase_gui.internal.screens.pages.categories

import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.common_gui.external.UsedeskViewCustomizer
import ru.usedesk.knowledgebase_gui.internal.screens.common.ViewModelFactory
import ru.usedesk.knowledgebase_gui.internal.screens.pages.FragmentListView
import ru.usedesk.knowledgebase_sdk.external.IUsedeskKnowledgeBase
import ru.usedesk.knowledgebase_sdk.external.UsedeskKnowledgeBaseSdk.instance
import ru.usedesk.knowledgebase_sdk.external.entity.UsedeskCategory

class CategoriesFragment : FragmentListView<UsedeskCategory?, CategoriesViewModel?>() {
    private val usedeskKnowledgeBaseSdk: IUsedeskKnowledgeBase
    override fun getViewModelFactory(): ViewModelFactory<CategoriesViewModel?>? {
        val categoryId = nonNullArguments.getLong(SECTION_ID_KEY)
        return CategoriesViewModel.Factory(usedeskKnowledgeBaseSdk, categoryId)
    }

    protected fun getAdapter(list: List<UsedeskCategory>?): RecyclerView.Adapter<*> {
        if (parentFragment !is IOnCategoryClickListener) {
            throw RuntimeException("Parent fragment must implement " +
                    IOnCategoryClickListener::class.java.simpleName)
        }
        return CategoriesAdapter(list!!, (parentFragment as IOnCategoryClickListener?)!!,
                UsedeskViewCustomizer.getInstance())
    }

    companion object {
        const val SECTION_ID_KEY = "sectionIdKey"
        fun newInstance(sectionId: Long): CategoriesFragment {
            val args = Bundle()
            args.putLong(SECTION_ID_KEY, sectionId)
            val fragment = CategoriesFragment()
            fragment.arguments = args
            return fragment
        }
    }

    init {
        usedeskKnowledgeBaseSdk = instance
    }
}