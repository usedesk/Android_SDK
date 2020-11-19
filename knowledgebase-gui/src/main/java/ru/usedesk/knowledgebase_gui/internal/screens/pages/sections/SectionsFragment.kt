package ru.usedesk.knowledgebase_gui.internal.screens.pages.sections

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.knowledgebase_gui.R
import ru.usedesk.knowledgebase_gui.internal.screens.entity.DataOrMessage
import ru.usedesk.knowledgebase_gui.internal.screens.pages.FragmentListView
import ru.usedesk.knowledgebase_sdk.external.entity.UsedeskSection

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
        @JvmOverloads
        fun newInstance(themeId: Int? = null): SectionsFragment {
            return SectionsFragment().apply {
                arguments = Bundle().apply {
                    if (themeId != null) {
                        putInt(THEME_ID_KEY, themeId)
                    }
                }
            }
        }
    }
}