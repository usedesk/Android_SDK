package ru.usedesk.chat_gui.chat.offlineformselector

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.chat_gui.R
import ru.usedesk.chat_gui.chat.offlineform.OfflineFormViewModel
import ru.usedesk.common_gui.UsedeskBinding
import ru.usedesk.common_gui.UsedeskFragment
import ru.usedesk.common_gui.inflateItem

internal class OfflineFormSelectorPage : UsedeskFragment() {

    private val viewModel: OfflineFormViewModel by viewModels(
        ownerProducer = {
            requireParentFragment().requireParentFragment()
        }
    )

    private lateinit var binding: Binding
    private lateinit var adapter: OfflineFormSelectorAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = inflateItem(
            inflater,
            container,
            R.layout.usedesk_page_offline_form_selector,
            R.style.Usedesk_Chat_Screen_Offline_Form_Selector_Page
        ) { rootView, defaultStyleId ->
            Binding(rootView, defaultStyleId)
        }

        init()

        return binding.rootView
    }

    private fun init() {
        adapter = OfflineFormSelectorAdapter(
            binding.rvItems,
            binding,
            viewModel,
            viewLifecycleOwner
        )
    }

    internal class Binding(rootView: View, defaultStyleId: Int) :
        UsedeskBinding(rootView, defaultStyleId) {
        val rvItems: RecyclerView = rootView.findViewById(R.id.rv_items)
    }
}