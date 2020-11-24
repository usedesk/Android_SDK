package ru.usedesk.chat_gui.internal.chat

import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import androidx.lifecycle.LifecycleOwner
import ru.usedesk.chat_gui.R
import ru.usedesk.common_gui.internal.visibleGone

class MessagePanelAdapter(parentView: View,
                          private val viewModel: ChatViewModel,
                          onClickAttach: View.OnClickListener,
                          lifecycleOwner: LifecycleOwner) {

    private val rootView: ViewGroup = parentView.findViewById(R.id.message_layout)
    private val messageEditText: EditText = parentView.findViewById(R.id.message_edit_text)
    private val attachFileImageButton: ImageButton = parentView.findViewById(R.id.attach_file_image_view)
    private val sendImageButton: ImageButton = parentView.findViewById(R.id.send_image_view)

    init {
        attachFileImageButton.setOnClickListener(onClickAttach)
        sendImageButton.setOnClickListener {
            onSendClick()
        }
        onMessagePanelState(viewModel.messagePanelStateLiveData.value)
        viewModel.messagePanelStateLiveData.observe(lifecycleOwner) {
            onMessagePanelState(it)
        }
        messageEditText.setText(viewModel.messageLiveData.value)
        messageEditText.addTextChangedListener(TextChangeListener {
            viewModel.onMessageChanged(it)
        })
    }

    private fun onMessagePanelState(messagePanelState: MessagePanelState?) {
        val messagePanel = (messagePanelState != null
                && messagePanelState == MessagePanelState.MESSAGE_PANEL)
        rootView.visibility = visibleGone(messagePanel)
        if (messagePanel) {
            messageEditText.setText(viewModel.messageLiveData.value)
        }
    }

    private fun onSendClick() {
        viewModel.onSend(messageEditText.text.toString().trim { it <= ' ' })
        messageEditText.setText("")
    }
}