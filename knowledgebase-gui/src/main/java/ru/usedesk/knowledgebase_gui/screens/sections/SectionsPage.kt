package ru.usedesk.knowledgebase_gui.screens.sections

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.common_gui.*
import ru.usedesk.common_gui.UsedeskCommonViewLoadingAdapter.State
import ru.usedesk.knowledgebase_gui.R
import ru.usedesk.knowledgebase_gui.screens.categories.CategoriesPage

internal class SectionsPage : UsedeskFragment() {

    private val viewModel: SectionsViewModel by viewModels()
    private lateinit var binding: Binding

    private lateinit var sectionsAdapter: SectionsAdapter
    private lateinit var loadingAdapter: UsedeskCommonViewLoadingAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = inflateItem(
            inflater,
            container,
            R.layout.usedesk_page_list,
            R.style.Usedesk_KnowledgeBase_Sections_Page
        ) { rootView, defaultStyleId ->
            Binding(rootView, defaultStyleId)
        }

        sectionsAdapter = SectionsAdapter(
            binding.rvItems,
            viewModel,
            viewLifecycleOwner
        ) { id, title ->
            findNavController().navigate(
                R.id.action_sectionsPage_to_categoriesPage,
                CategoriesPage.createBundle(title, id)
            )
        }

        loadingAdapter = UsedeskCommonViewLoadingAdapter(binding.vLoading)

        viewModel.modelLiveData.initAndObserveWithOld(viewLifecycleOwner) { old, new ->
            if (old?.state != new.state) {
                loadingAdapter.update(new.state)
                binding.rvItems.visibility = visibleInvisible(new.state == State.LOADED)
            }
        }

        return binding.rootView
    }

    internal class Binding(rootView: View, defaultStyleId: Int) :
        UsedeskBinding(rootView, defaultStyleId) {
        val rvItems: RecyclerView = rootView.findViewById(R.id.rv_items)
        val vLoading = UsedeskCommonViewLoadingAdapter.Binding(
            rootView.findViewById(R.id.v_loading),
            defaultStyleId
        )
    }
}