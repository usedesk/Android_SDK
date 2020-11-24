package ru.usedesk.chat_gui.internal.chat

import android.util.Patterns
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import ru.usedesk.chat_gui.R
import ru.usedesk.common_gui.internal.visibleGone

class OfflineFormExpectedAdapter(parentView: View, private val viewModel: ChatViewModel,
                                 lifecycleOwner: LifecycleOwner) {
    private val rootView: ViewGroup = parentView.findViewById(R.id.offline_form_layout)
    private val nameEditText: EditText = parentView.findViewById(R.id.offline_form_name_edit_text)
    private val emailEditText: EditText = parentView.findViewById(R.id.offline_form_email_edit_text)
    private val messageEditText: EditText = parentView.findViewById(R.id.offline_form_message_edit_text)
    private val sendTextView: TextView = parentView.findViewById(R.id.usedesk_offline_form_send_image_view)

    init {
        sendTextView.setOnClickListener {
            onSend()
        }

        updateFields()

        onMessagePanelState(viewModel.messagePanelStateLiveData.value)

        viewModel.messagePanelStateLiveData.observe(lifecycleOwner) {
            onMessagePanelState(it)
        }
        viewModel.messageLiveData.observe(lifecycleOwner) {
            validateFields()
        }
        viewModel.nameLiveData.observe(lifecycleOwner) {
            validateFields()
        }
        viewModel.emailLiveData.observe(lifecycleOwner) {
            validateFields()
        }

        messageEditText.addTextChangedListener(TextChangeListener {
            viewModel.onMessageChanged(it)
        })
        nameEditText.addTextChangedListener(TextChangeListener {
            viewModel.onNameChanged(it)
        })
        emailEditText.addTextChangedListener(TextChangeListener {
            viewModel.onEmailChanged(it)
        })
    }

    private fun validateFields() {
        val name = nameEditText.text.toString()
        val email = emailEditText.text.toString()
        val message = messageEditText.text.toString()

        val nameCorrect = name.isNotEmpty()
        val emailCorrect = Patterns.EMAIL_ADDRESS.matcher(email).matches()
        val messageCorrect = message.isNotEmpty()

        sendTextView.isEnabled = nameCorrect && emailCorrect && messageCorrect
    }

    private fun onMessagePanelState(messagePanelState: MessagePanelState?) {
        val offlineFormExpected = (messagePanelState != null
                && messagePanelState == MessagePanelState.OFFLINE_FORM_EXPECTED)
        rootView.visibility = visibleGone(offlineFormExpected)
        if (offlineFormExpected) {
            updateFields()
        }
    }

    private fun updateFields() {
        messageEditText.setText(viewModel.messageLiveData.value)
        nameEditText.setText(viewModel.nameLiveData.value)
        emailEditText.setText(viewModel.emailLiveData.value)
    }

    private fun onSend() {
        viewModel.onSend(nameEditText.text.toString(),
                emailEditText.text.toString(),
                messageEditText.text.toString())
    }
}