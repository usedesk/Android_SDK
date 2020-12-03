package ru.usedesk.knowledgebase_gui.pages.sections

import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.knowledgebase_gui.R
import ru.usedesk.knowledgebase_gui.entity.DataOrMessage
import ru.usedesk.knowledgebase_gui.pages.FragmentListView
import ru.usedesk.knowledgebase_sdk.entity.UsedeskSection

class SectionsFragment : FragmentListView<UsedeskSection>(
        R.layout.usedesk_fragment_list,
        R.style.Usedesk_Theme_KnowledgeBase
) {
    private val viewModel: SectionsViewModel by viewModels()

    override fun getLiveData(): LiveData<DataOrMessage<List<UsedeskSection>>> = viewModel.liveData

    override fun getAdapter(list: List<UsedeskSection>): RecyclerView.Adapter<*> {
        if (parentFragment !is IOnSectionClickListener) {
            throw RuntimeException("Parent fragment must implement " +
                    IOnSectionClickListener::class.java.simpleName)
        }
        return SectionsAdapter(list, (parentFragment as IOnSectionClickListener))
    }

    companion object {
        fun newInstance(): SectionsFragment {
            return SectionsFragment()
        }
    }
}