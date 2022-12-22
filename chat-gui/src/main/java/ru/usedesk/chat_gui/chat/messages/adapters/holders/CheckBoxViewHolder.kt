package ru.usedesk.chat_gui.chat.messages.adapters.holders

import android.text.Html
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.usedesk.chat_gui.R
import ru.usedesk.chat_gui.chat.messages.MessagesViewModel
import ru.usedesk.chat_gui.chat.messages.MessagesViewModel.Event
import ru.usedesk.chat_gui.chat.messages.adapters.MessageFormsAdapter
import ru.usedesk.chat_gui.chat.messages.adapters.MessageFormsAdapter.Item
import ru.usedesk.chat_gui.chat.messages.adapters.MessageFormsAdapter.Item.ItemCheckBox
import ru.usedesk.chat_sdk.entity.UsedeskForm
import ru.usedesk.chat_sdk.entity.UsedeskMessageAgentText.Field

internal class CheckBoxViewHolder(
    private val binding: MessageFormsAdapter.CheckBoxBinding,
    private val onEvent: (Event) -> Unit
) : BaseViewHolder(binding.rootView) {

    override fun bind(
        messageId: Long,
        item: Item,
        scope: CoroutineScope,
        stateFlow: StateFlow<MessagesViewModel.State>
    ) {
        item as ItemCheckBox
        var checkbox: Field.CheckBox? = null
        var formState: UsedeskForm.State? = null
        stateFlow.onEach { state ->
            val form = state.formMap[messageId]
            if (form != null) {
                val newCheckbox =
                    form.fields.firstOrNull { it.id == item.fieldId } as Field.CheckBox
                val newFormState = form.state
                if (checkbox != newCheckbox || formState != newFormState) {
                    checkbox = newCheckbox
                    formState = newFormState
                    update(
                        messageId,
                        newCheckbox,
                        newFormState
                    )
                }
            }
        }.launchIn(viewHolderScope)
    }

    private fun update(
        messageId: Long,
        checkBox: Field.CheckBox,
        state: UsedeskForm.State
    ) {
        binding.tvText.text = Html.fromHtml(
            checkBox.name + when {
                checkBox.required -> REQUIRED_POSTFIX_HTML
                else -> ""
            }
        )
        binding.ivChecked.setImageResource(
            when {
                checkBox.checked -> R.drawable.usedesk_ic_form_checked
                checkBox.hasError -> R.drawable.usedesk_ic_form_unchecked_error
                else -> R.drawable.usedesk_ic_form_unchecked
            }
        )
        val enabled = state == UsedeskForm.State.LOADED
        binding.ivChecked.isClickable = enabled
        binding.ivChecked.isFocusable = enabled
        binding.ivChecked.setOnClickListener {
            val newCheckbox = checkBox.copy(
                checked = !checkBox.checked,
                hasError = false
            )
            onEvent(
                Event.FormChanged(
                    messageId,
                    newCheckbox
                )
            )
        }
    }
}