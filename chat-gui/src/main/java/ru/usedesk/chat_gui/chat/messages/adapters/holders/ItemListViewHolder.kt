package ru.usedesk.chat_gui.chat.messages.adapters.holders

import ru.usedesk.chat_gui.R
import ru.usedesk.chat_gui.chat.messages.MessagesViewModel.Event
import ru.usedesk.chat_gui.chat.messages.adapters.MessageFormsAdapter
import ru.usedesk.chat_gui.chat.messages.adapters.MessageFormsAdapter.Item
import ru.usedesk.chat_gui.chat.messages.adapters.MessageFormsAdapter.Item.ItemList
import ru.usedesk.chat_sdk.entity.UsedeskForm

internal class ItemListViewHolder(
    private val binding: MessageFormsAdapter.ItemListBinding,
    private val onEvent: (Event) -> Unit
) : BaseViewHolder(binding.rootView) {

    override fun bind(
        messageId: Long,
        item: Item,
        state: UsedeskForm.State
    ) {
        item as ItemList
        binding.tvText.apply {
            when (val selected = item.list.selected) {
                null -> {
                    text = item.list.name
                    setTextColor(binding.rootView.resources.getColor(R.color.usedesk_gray_2)) //TODO: styleValues
                }
                else -> {
                    text = selected.name
                    setTextColor(binding.rootView.resources.getColor(R.color.usedesk_black_3)) //TODO: styleValues
                }
            }
        }
        binding.lClickable.setOnClickListener {
            onEvent(
                Event.FormListClicked(
                    messageId,
                    item.list
                )
            )
        }
    }
}