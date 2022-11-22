package ru.usedesk.chat_gui.chat.messages.adapters.holders

import androidx.core.widget.addTextChangedListener
import ru.usedesk.chat_gui.chat.messages.MessagesViewModel.Event
import ru.usedesk.chat_gui.chat.messages.MessagesViewModel.FormState
import ru.usedesk.chat_gui.chat.messages.adapters.MessageItemsAdapter
import ru.usedesk.chat_sdk.entity.UsedeskMessageAgentText.Form

internal class TextViewHolder(
    private val binding: MessageItemsAdapter.TextBinding,
    private val onEvent: (Event) -> Unit
) : BaseViewHolder<Form.Field.Text, FormState.Text>(binding.rootView) {

    private var onTextChangedListener: (String) -> Unit = {}

    init {
        binding.etText.addTextChangedListener {
            onTextChangedListener(it?.toString() ?: "")
        }
    }

    override fun bind(
        messageId: Long,
        form: Form.Field.Text,
        formState: FormState.Text
    ) {
        binding.etText.run {
            hint = form.name
            onTextChangedListener = {}
            setText(formState.text)
            onTextChangedListener = {
                onEvent(Event.FormChanged(form, formState.copy(text = it)))
            }
        }
    }
}