package ru.usedesk.chat_gui.chat.messages.adapters.holders

import androidx.core.widget.addTextChangedListener
import ru.usedesk.chat_gui.chat.messages.MessagesViewModel.Event
import ru.usedesk.chat_gui.chat.messages.MessagesViewModel.FormItemState
import ru.usedesk.chat_gui.chat.messages.adapters.MessageItemsAdapter
import ru.usedesk.chat_sdk.entity.UsedeskMessageAgentText.Form

internal class TextViewHolder(
    private val binding: MessageItemsAdapter.TextBinding,
    private val onEvent: (Event) -> Unit
) :
    BaseViewHolder<Form.Field.Text, FormItemState.Text>(binding.rootView) {
    private var onTextChangedListener: (String) -> Unit = {}

    init {
        binding.etText.addTextChangedListener {
            onTextChangedListener(it?.toString() ?: "")
        }
    }

    override fun bind(
        messageId: Long,
        form: Form.Field.Text,
        formItemState: FormItemState.Text
    ) {
        binding.etText.run {
            hint = form.name
            onTextChangedListener = {}
            setText(formItemState.text)
            onTextChangedListener = {
                onEvent(Event.FormChanged(form, formItemState.copy(text = it)))
            }
        }
    }
}