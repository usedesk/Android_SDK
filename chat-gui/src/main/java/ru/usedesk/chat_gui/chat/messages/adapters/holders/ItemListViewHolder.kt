package ru.usedesk.chat_gui.chat.messages.adapters.holders

import ru.usedesk.chat_gui.chat.messages.MessagesViewModel.Event
import ru.usedesk.chat_gui.chat.messages.MessagesViewModel.FormItemState
import ru.usedesk.chat_gui.chat.messages.adapters.MessageItemsAdapter
import ru.usedesk.chat_sdk.entity.UsedeskMessageAgentText.Form

internal class ItemListViewHolder(
    private val binding: MessageItemsAdapter.ItemListBinding,
    private val onEvent: (Event) -> Unit
) :
    BaseViewHolder<Form.Field.List, FormItemState.ItemList>(binding.rootView) {

    override fun bind(
        messageId: Long,
        form: Form.Field.List,
        formItemState: FormItemState.ItemList
    ) {
        val name = when {
            form.items.isNotEmpty() -> formItemState.selected
                .joinToString(separator = ", ") { it.name }
                .ifEmpty { null }
            else -> null
        }
        binding.rootView.setOnClickListener {
            onEvent(Event.FormListClicked(form, formItemState))
        }
        binding.tvText.text = name ?: form.name
        //binding.tvText.setTextColor() //TODO: цвет текста
    }
}