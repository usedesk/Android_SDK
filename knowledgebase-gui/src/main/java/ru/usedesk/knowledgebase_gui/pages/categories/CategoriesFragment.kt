package ru.usedesk.knowledgebase_gui.pages.categories

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

internal class CategoriesFragment : UsedeskFragment() {

    private val viewModel: CategoriesViewModel by viewModels()

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

        val sectionId = argsGetLong(SECTION_ID_KEY)
        if (sectionId != null) {
            init(sectionId)
        }

        return binding.rootView
    }

    fun init(sectionId: Long) {
        viewModel.init(sectionId)

        CategoriesAdapter(binding.rvItems,
                viewLifecycleOwner,
                viewModel) {
            getParentListener<IOnCategoryClickListener>()?.onCategoryClick(it)
        }
    }

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

    internal class Binding(rootView: View, defaultStyleId: Int) : UsedeskBinding(rootView, defaultStyleId) {
        val rvItems: RecyclerView = rootView.findViewById(R.id.rv_items)
        val tvLoading: TextView = rootView.findViewById(R.id.tv_loading)
    }
}