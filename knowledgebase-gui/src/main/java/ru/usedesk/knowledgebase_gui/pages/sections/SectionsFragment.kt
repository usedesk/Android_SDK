package ru.usedesk.knowledgebase_gui.pages.sections

import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.common_gui.UsedeskBinding
import ru.usedesk.knowledgebase_gui.R
import ru.usedesk.knowledgebase_gui.entity.DataOrMessage
import ru.usedesk.knowledgebase_gui.pages.FragmentListView
import ru.usedesk.knowledgebase_sdk.entity.UsedeskSection

internal class SectionsFragment : FragmentListView<UsedeskSection, SectionsFragment.Binding>(
        R.layout.usedesk_fragment_list,
        R.style.Usedesk_KnowledgeBase
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

    override fun createBinding(rootView: View) = Binding(rootView)

    companion object {
        fun newInstance(): SectionsFragment {
            return SectionsFragment()
        }
    }

    internal class Binding(rootView: View) : UsedeskBinding(rootView) {

    }
}