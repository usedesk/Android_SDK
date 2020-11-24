package ru.usedesk.chat_gui.internal.chat

import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import ru.usedesk.chat_gui.R
import ru.usedesk.common_gui.internal.visibleGone

class OfflineFormSentAdapter(parentView: View,
                             viewModel: ChatViewModel,
                             lifecycleOwner: LifecycleOwner) {

    private val rootView: ViewGroup = parentView.findViewById(R.id.offline_form_sent_layout)

    private fun onMessagePanelState(messagePanelState: MessagePanelState?) {
        val messagePanel = messagePanelState != null
                && messagePanelState == MessagePanelState.OFFLINE_FORM_SENT
        rootView.visibility = visibleGone(messagePanel)
    }

    init {
        onMessagePanelState(viewModel.messagePanelStateLiveData.value)
        viewModel.messagePanelStateLiveData.observe(lifecycleOwner) {
            onMessagePanelState(it)
        }
    }
}