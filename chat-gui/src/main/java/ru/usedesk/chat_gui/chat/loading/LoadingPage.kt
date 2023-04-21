
package ru.usedesk.chat_gui.chat.loading

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ru.usedesk.chat_gui.R
import ru.usedesk.chat_gui.chat.loading.LoadingViewModel.Page
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflateItem(
        inflater,
        container,
        R.layout.usedesk_page_loading,
        R.style.Usedesk_Chat_Screen_Loading_Page,
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
                    R.id.dest_loadingPage,
                    when (page) {
                        Page.OFFLINE_FORM -> R.id.action_loadingPage_to_offlineFormPage
                        Page.MESSAGES -> R.id.action_loadingPage_to_messagesPage
                    }
                )
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