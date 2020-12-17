package ru.usedesk.knowledgebase_gui.pages.categories

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.common_gui.UsedeskBinding
import ru.usedesk.knowledgebase_gui.R
import ru.usedesk.knowledgebase_gui.entity.DataOrMessage
import ru.usedesk.knowledgebase_gui.pages.FragmentListView
import ru.usedesk.knowledgebase_sdk.entity.UsedeskCategory

internal class CategoriesFragment : FragmentListView<UsedeskCategory, CategoriesFragment.Binding>(
        R.layout.usedesk_fragment_list,
        R.style.Usedesk_KnowledgeBase
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

    override fun createBinding(rootView: View) = Binding(rootView)

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

    internal class Binding(rootView: View) : UsedeskBinding(rootView) {

    }
}