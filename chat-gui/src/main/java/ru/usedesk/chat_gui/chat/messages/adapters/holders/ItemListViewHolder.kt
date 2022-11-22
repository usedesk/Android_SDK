package ru.usedesk.chat_gui.chat.messages.adapters.holders

import ru.usedesk.chat_gui.chat.messages.MessagesViewModel.Event
import ru.usedesk.chat_gui.chat.messages.MessagesViewModel.FormState
import ru.usedesk.chat_gui.chat.messages.adapters.MessageItemsAdapter
import ru.usedesk.chat_sdk.entity.UsedeskMessageAgentText.Form

internal class ItemListViewHolder(
    private val binding: MessageItemsAdapter.ItemListBinding,
    private val onEvent: (Event) -> Unit
) : BaseViewHolder<Form.Field.List, FormState.List>(binding.rootView) {

    override fun bind(
        messageId: Long,
        form: Form.Field.List,
        formState: FormState.List
    ) {
        val name = when {
            form.items.isNotEmpty() -> formState.selected
                .joinToString(separator = ", ") { it.name }
                .ifEmpty { null }
            else -> null
        }
        binding.rootView.setOnClickListener {
            onEvent(Event.FormListClicked(form, formState))
        }
        binding.tvText.text = name ?: form.name
        //binding.tvText.setTextColor() //TODO: цвет текста
    }
}