package ru.usedesk.chat_gui.chat.messages.adapters.holders

import ru.usedesk.chat_gui.R
import ru.usedesk.chat_gui.chat.messages.MessagesViewModel.Event
import ru.usedesk.chat_gui.chat.messages.adapters.MessageFormsAdapter
import ru.usedesk.chat_gui.chat.messages.adapters.MessageFormsAdapter.Item
import ru.usedesk.chat_gui.chat.messages.adapters.MessageFormsAdapter.Item.ItemCheckBox
import ru.usedesk.chat_sdk.entity.UsedeskForm
import ru.usedesk.chat_sdk.entity.UsedeskMessageAgentText

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
        binding.tvText.text = item.checkBox.name + when {
            item.checkBox.required -> " *"
            else -> ""
        }
        updateChecked(messageId, item.checkBox)
    }

    private fun updateChecked(
        messageId: Long,
        checkBox: UsedeskMessageAgentText.Field.CheckBox
    ) {
        binding.ivChecked.setImageResource(
            when {
                checkBox.checked -> R.drawable.usedesk_ic_form_checked
                else -> R.drawable.usedesk_ic_form_unchecked
            }
        )
        binding.ivChecked.setOnClickListener {
            val newCheckbox = checkBox.copy(
                checked = !checkBox.checked,
                hasError = false
            )
            updateChecked(messageId, newCheckbox)
            onEvent(
                Event.FormChanged(
                    messageId,
                    newCheckbox
                )
            )
        }
    }
}