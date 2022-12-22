package ru.usedesk.chat_gui.chat.messages.adapters.holders

import android.view.View
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import ru.usedesk.chat_gui.chat.messages.MessagesViewModel
import ru.usedesk.chat_gui.chat.messages.MessagesViewModel.Event
import ru.usedesk.chat_gui.chat.messages.adapters.MessageFormsAdapter.ButtonBinding
import ru.usedesk.chat_gui.chat.messages.adapters.MessageFormsAdapter.Item
import ru.usedesk.chat_gui.chat.messages.adapters.MessageFormsAdapter.Item.ItemButton

internal class ButtonViewHolder(
    private val binding: ButtonBinding,
    private val onEvent: (Event) -> Unit
) : BaseViewHolder(binding.rootView) {

    init {
        binding.pbLoading.visibility = View.INVISIBLE
    }

    override fun bind(
        messageId: Long,
        item: Item,
        scope: CoroutineScope,
        stateFlow: StateFlow<MessagesViewModel.State>
    ) {
        item as ItemButton
        binding.tvTitle.run {
            text = item.button.name
            setOnClickListener { onEvent(Event.MessageButtonClick(item.button)) }
        }
    }
}