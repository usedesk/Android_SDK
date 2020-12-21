package ru.usedesk.knowledgebase_gui.pages.sections

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.common_gui.UsedeskBinding
import ru.usedesk.common_gui.UsedeskFragment
import ru.usedesk.common_gui.inflateItem
import ru.usedesk.knowledgebase_gui.R

internal class SectionsFragment : UsedeskFragment() {

    private val viewModel: SectionsViewModel by viewModels()
    private lateinit var binding: Binding

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        binding = inflateItem(inflater,
                container,
                R.layout.usedesk_fragment_list,
                R.style.Usedesk_KnowledgeBase) { rootView, defaultStyleId ->
            Binding(rootView, defaultStyleId)
        }

        init()

        return binding.rootView
    }

    private fun init() {
        SectionsAdapter(viewModel, binding.rvSections) {
            getParentListener<IOnSectionClickListener>()?.onSectionClick(it)
        }


    }

    companion object {
        fun newInstance(): SectionsFragment {
            return SectionsFragment()
        }
    }

    internal class Binding(rootView: View, defaultStyleId: Int) : UsedeskBinding(rootView, defaultStyleId) {
        val rvSections: RecyclerView = rootView.findViewById(R.id.rv_items)
        val tvLoading: TextView = rootView.findViewById(R.id.tv_loading)
    }
}