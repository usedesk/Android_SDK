package ru.usedesk.knowledgebase_gui.screens.sections

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ru.usedesk.common_gui.UsedeskBinding
import ru.usedesk.common_gui.UsedeskFragment
import ru.usedesk.common_gui.inflateItem
import ru.usedesk.common_gui.showInstead
import ru.usedesk.knowledgebase_gui.IUsedeskOnSupportClickListener
import ru.usedesk.knowledgebase_gui.R

internal class SectionsPage : UsedeskFragment() {

    private val viewModel: SectionsViewModel by viewModels()
    private lateinit var binding: Binding

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = inflateItem(inflater,
                container,
                R.layout.usedesk_page_list,
                R.style.Usedesk_KnowledgeBase_Sections_Page) { rootView, defaultStyleId ->
            Binding(rootView, defaultStyleId)
        }.apply {
            btnSupport.setOnClickListener {
                requireParentFragment().also {
                    if (it is IUsedeskOnSupportClickListener) {
                        it.onSupportClick()
                    }
                }
            }
        }

        SectionsAdapter(binding.rvItems,
                viewLifecycleOwner,
                viewModel) { id, title ->
            getParentListener<IOnSectionClickListener>()?.onSectionClick(id, title)
        }

        viewModel.sectionsLiveData.observe(viewLifecycleOwner) {
            if (it != null) {
                showInstead(binding.rvItems, binding.pbLoading)
            }
        }

        return binding.rootView
    }

    companion object {
        fun newInstance(): SectionsPage {
            return SectionsPage()
        }
    }

    internal class Binding(rootView: View, defaultStyleId: Int) : UsedeskBinding(rootView, defaultStyleId) {
        val rvItems: RecyclerView = rootView.findViewById(R.id.rv_items)
        val pbLoading: ProgressBar = rootView.findViewById(R.id.pb_loading)
        val btnSupport: FloatingActionButton = rootView.findViewById(R.id.fab_support)
    }
}