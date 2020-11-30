package ru.usedesk.knowledgebase_gui.internal.screens.pages.categories

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.knowledgebase_gui.R
import ru.usedesk.knowledgebase_gui.internal.screens.entity.DataOrMessage
import ru.usedesk.knowledgebase_gui.internal.screens.pages.FragmentListView
import ru.usedesk.knowledgebase_sdk.external.entity.UsedeskCategory

class CategoriesFragment : FragmentListView<UsedeskCategory>(
        R.layout.usedesk_fragment_list,
        R.style.Usedesk_Theme_KnowledgeBase
) {

    private val viewModel: CategoriesViewModel by viewModels()

    override fun getAdapter(list: List<UsedeskCategory>): RecyclerView.Adapter<*> {
        if (parentFragment !is IOnCategoryClickListener) {
            throw RuntimeException("Parent fragment must implement " +
                    IOnCategoryClickListener::class.java.simpleName)
        }
        return CategoriesAdapter(list, parentFragment as IOnCategoryClickListener)
    }

    override fun init() {
        val sectionId = argsGetLong(SECTION_ID_KEY)
        if (sectionId != null) {
            viewModel.init(sectionId)
        }
    }

    override fun getLiveData(): LiveData<DataOrMessage<List<UsedeskCategory>>> = viewModel.liveData

    companion object {
        private const val SECTION_ID_KEY = "sectionIdKey"

        fun newInstance(sectionId: Long): CategoriesFragment {
            return CategoriesFragment().apply {
                arguments = Bundle().apply {
                    putLong(SECTION_ID_KEY, sectionId)
                }
            }
        }
    }
}