package ru.usedesk.chat_gui.chat.loading

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ru.usedesk.chat_gui.chat.loading.LoadingViewModel.Page
import ru.usedesk.chat_gui.chat.requireChatViewModelStoreOwner
import ru.usedesk.common_gui.UsedeskBinding
import ru.usedesk.common_gui.UsedeskCommonViewLoadingAdapter
import ru.usedesk.common_gui.UsedeskFragment
import ru.usedesk.common_gui.inflateItem
import ru.usedesk.chat_gui.R as chatR

internal class LoadingPage : UsedeskFragment() {

    private val viewModel: LoadingViewModel by viewModels(
        ownerProducer = this@LoadingPage::requireChatViewModelStoreOwner
    )

    private lateinit var loadingAdapter: UsedeskCommonViewLoadingAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflateItem(
        inflater,
        container,
        chatR.layout.usedesk_page_loading,
        chatR.style.Usedesk_Chat_Screen_Loading_Page,
        ::Binding
    ).apply {
        loadingAdapter = UsedeskCommonViewLoadingAdapter(vLoadingBinding)
    }.rootView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.modelFlow.onEachWithOld { old, new ->
            if (old?.state != new.state) {
                loadingAdapter.update(new.state)
            }
            new.goNext?.use { page ->
                findNavController().navigateSafe(
                    chatR.id.dest_loadingPage,
                    when (page) {
                        Page.OFFLINE_FORM -> chatR.id.action_loadingPage_to_offlineFormPage
                        Page.MESSAGES -> chatR.id.action_loadingPage_to_messagesPage
                    }
                )
            }
        }
    }

    class Binding(rootView: View, defaultStyleId: Int) :
        UsedeskBinding(rootView, defaultStyleId) {

        val vLoadingBinding = UsedeskCommonViewLoadingAdapter.Binding(
            rootView.findViewById(chatR.id.v_loading),
            defaultStyleId
        )
    }
}