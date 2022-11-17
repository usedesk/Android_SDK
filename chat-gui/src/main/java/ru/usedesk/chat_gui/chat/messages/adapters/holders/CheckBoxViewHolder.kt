package ru.usedesk.chat_gui.chat.messages.adapters.holders

import ru.usedesk.chat_gui.chat.messages.MessagesViewModel.Event
import ru.usedesk.chat_gui.chat.messages.MessagesViewModel.FormItemState
import ru.usedesk.chat_gui.chat.messages.adapters.MessageItemsAdapter
import ru.usedesk.chat_sdk.entity.UsedeskMessageAgentText.Form

internal class CheckBoxViewHolder(
    private val binding: MessageItemsAdapter.CheckBoxBinding,
    private val onEvent: (Event) -> Unit
) :
    BaseViewHolder<Form.Field.CheckBox, FormItemState.CheckBox>(binding.rootView) {

    override fun bind(
        messageId: Long,
        form: Form.Field.CheckBox,
        formItemState: FormItemState.CheckBox
    ) {
        binding.tvText.text = form.name
        binding.rootView.setOnClickListener {
            onEvent(
                Event.FormChanged(
                    form,
                    formItemState.copy(checked = !formItemState.checked)
                )
            )
        }
    }
}