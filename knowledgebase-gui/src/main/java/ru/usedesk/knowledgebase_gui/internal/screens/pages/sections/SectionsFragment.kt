package ru.usedesk.knowledgebase_gui.internal.screens.pages.sections

import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.common_gui.external.UsedeskViewCustomizer
import ru.usedesk.knowledgebase_gui.internal.screens.common.ViewModelFactory
import ru.usedesk.knowledgebase_gui.internal.screens.pages.FragmentListView
import ru.usedesk.knowledgebase_sdk.external.IUsedeskKnowledgeBase
import ru.usedesk.knowledgebase_sdk.external.UsedeskKnowledgeBaseSdk.instance
import ru.usedesk.knowledgebase_sdk.external.entity.UsedeskSection

class SectionsFragment : FragmentListView<UsedeskSection?, SectionsViewModel?>() {
    private val usedeskKnowledgeBaseSdk: IUsedeskKnowledgeBase
    override fun getViewModelFactory(): ViewModelFactory<SectionsViewModel?>? {
        return SectionsViewModel.Factory(usedeskKnowledgeBaseSdk)
    }

    protected fun getAdapter(list: List<UsedeskSection>?): RecyclerView.Adapter<*> {
        if (parentFragment !is IOnSectionClickListener) {
            throw RuntimeException("Parent fragment must implement " +
                    IOnSectionClickListener::class.java.simpleName)
        }
        return SectionsAdapter(list!!, (parentFragment as IOnSectionClickListener?)!!,
                UsedeskViewCustomizer.getInstance())
    }

    companion object {
        fun newInstance(): SectionsFragment {
            return SectionsFragment()
        }
    }

    init {
        usedeskKnowledgeBaseSdk = instance
    }
}