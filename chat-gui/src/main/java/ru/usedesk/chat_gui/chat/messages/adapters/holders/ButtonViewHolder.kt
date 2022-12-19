package ru.usedesk.chat_gui.chat.messages.adapters.holders

import android.view.View
import ru.usedesk.chat_gui.chat.messages.MessagesViewModel.Event
import ru.usedesk.chat_gui.chat.messages.adapters.MessageFormsAdapter
import ru.usedesk.chat_gui.chat.messages.adapters.MessageFormsAdapter.ButtonBinding
import ru.usedesk.chat_gui.chat.messages.adapters.MessageFormsAdapter.Item.ItemButton
import ru.usedesk.chat_sdk.entity.UsedeskForm
import ru.usedesk.common_gui.visibleInvisible

internal class ButtonViewHolder(
    private val binding: ButtonBinding,
    private val onEvent: (Event) -> Unit
) : BaseViewHolder(binding.rootView) {

    override fun bind(
        messageId: Long,
        item: MessageFormsAdapter.Item,
        state: UsedeskForm.State
    ) {
        item as ItemButton
        binding.tvTitle.run {
            when (item.button) {
                null -> {
                    val loading = state == UsedeskForm.State.SENDING
                    isEnabled = !loading
                    isClickable = !loading
                    isFocusable = !loading
                    text = "АПЛАЙ" //TODO:TEMP
                    setOnClickListener { onEvent(Event.FormApplyClick(messageId)) }
                    binding.pbLoading.visibility = visibleInvisible(loading)
                }
                else -> {
                    isEnabled = true
                    isClickable = true
                    isFocusable = true
                    text = item.button.name
                    setOnClickListener { onEvent(Event.MessageButtonClick(item.button)) }
                    binding.pbLoading.visibility = View.INVISIBLE
                }
            }
        }
    }
}