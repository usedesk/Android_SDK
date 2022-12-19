package ru.usedesk.chat_gui.chat.messages.adapters.holders

import ru.usedesk.chat_gui.chat.messages.MessagesViewModel.Event
import ru.usedesk.chat_gui.chat.messages.adapters.MessageFormsAdapter
import ru.usedesk.chat_gui.chat.messages.adapters.MessageFormsAdapter.Item
import ru.usedesk.chat_gui.chat.messages.adapters.MessageFormsAdapter.Item.ItemCheckBox
import ru.usedesk.chat_sdk.entity.UsedeskForm

internal class CheckBoxViewHolder(
    private val binding: MessageFormsAdapter.CheckBoxBinding,
    private val onEvent: (Event) -> Unit
) : BaseViewHolder(binding.rootView) {

    override fun bind(
        messageId: Long,
        item: Item,
        state: UsedeskForm.State
    ) {
        item as ItemCheckBox
        binding.tvText.text = item.checkBox.name
        binding.rootView.setOnClickListener {
            onEvent(
                Event.FormChanged(
                    messageId,
                    item.checkBox.copy(
                        checked = !item.checkBox.checked,
                        hasError = false
                    )
                )
            )
        }
    }
}