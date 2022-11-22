package ru.usedesk.chat_gui.chat.messages.adapters.holders

import ru.usedesk.chat_gui.R
import ru.usedesk.chat_gui.chat.messages.MessagesViewModel.Event
import ru.usedesk.chat_gui.chat.messages.MessagesViewModel.FormState
import ru.usedesk.chat_gui.chat.messages.adapters.MessageItemsAdapter.ButtonBinding
import ru.usedesk.chat_sdk.entity.UsedeskMessageAgentText.Form

internal class ButtonViewHolder(
    private val binding: ButtonBinding,
    private val onEvent: (Event) -> Unit,
    private val onClick: (Form.Button) -> Unit
) : BaseViewHolder<Form.Button, FormState.Button>(binding.rootView) {

    override fun bind(
        messageId: Long,
        form: Form.Button,
        formState: FormState.Button
    ) {
        binding.tvTitle.run {
            text = when (form.id) {
                Form.Button.FORM_APPLY_BUTTON_ID -> "АПЛАЙ" //TODO:TEMP
                else -> form.name
            }
            setBackgroundColor(
                binding.rootView.resources.getColor(
                    when {
                        !formState.enabled -> R.color.usedesk_gray_2
                        else -> R.color.usedesk_black_2
                    }
                )
            )
            isEnabled = formState.enabled
            isClickable = formState.enabled
            isFocusable = formState.enabled
            setOnClickListener {
                when (form.id) {
                    Form.Button.FORM_APPLY_BUTTON_ID -> onEvent(Event.FormApplyClick(messageId))
                    else -> onClick(form)
                }
            }
        }
    }
}