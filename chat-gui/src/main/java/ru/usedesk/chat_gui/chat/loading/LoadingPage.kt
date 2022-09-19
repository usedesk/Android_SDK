package ru.usedesk.chat_gui.chat.loading

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ru.usedesk.chat_gui.R
import ru.usedesk.chat_gui.chat.requireChatViewModelStoreOwner
import ru.usedesk.common_gui.UsedeskBinding
import ru.usedesk.common_gui.UsedeskCommonViewLoadingAdapter
import ru.usedesk.common_gui.UsedeskFragment
import ru.usedesk.common_gui.inflateItem

internal class LoadingPage : UsedeskFragment() {

    private val viewModel: LoadingViewModel by viewModels(
        ownerProducer = this@LoadingPage::requireChatViewModelStoreOwner
    )

    private lateinit var loadingAdapter: UsedeskCommonViewLoadingAdapter

    private lateinit var binding: Binding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = inflateItem(
            inflater,
            container,
            R.layout.usedesk_page_loading,
            R.style.Usedesk_Chat_Screen_Loading_Page
        ) { rootView, defaultStyleId ->
            Binding(rootView, defaultStyleId)
        }

        loadingAdapter = UsedeskCommonViewLoadingAdapter(binding.vLoadingBinding)

        return binding.rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.modelFlow.onEachWithOld { old, new ->
            if (old?.state != new.state) {
                loadingAdapter.update(new.state)
            }
            new.goNext.process { page ->
                when (page) {
                    LoadingViewModel.Page.OFFLINE_FORM -> {
                        findNavController().navigate(R.id.action_loadingPage_to_offlineFormPage)
                    }
                    LoadingViewModel.Page.MESSAGES -> {
                        findNavController().navigate(R.id.action_loadingPage_to_messagesPage)
                    }
                }
            }
        }
    }

    class Binding(rootView: View, defaultStyleId: Int) :
        UsedeskBinding(rootView, defaultStyleId) {

        val vLoadingBinding = UsedeskCommonViewLoadingAdapter.Binding(
            rootView.findViewById(R.id.v_loading),
            defaultStyleId
        )
    }
}