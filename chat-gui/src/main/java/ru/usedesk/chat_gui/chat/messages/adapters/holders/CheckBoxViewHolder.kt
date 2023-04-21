
package ru.usedesk.chat_gui.chat.messages.adapters.holders

import android.text.Html
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.usedesk.chat_gui.R
import ru.usedesk.chat_gui.chat.messages.MessagesViewModel
import ru.usedesk.chat_gui.chat.messages.MessagesViewModel.Event
import ru.usedesk.chat_gui.chat.messages.adapters.MessageFormAdapter
import ru.usedesk.chat_gui.chat.messages.adapters.MessageFormAdapter.Item
import ru.usedesk.chat_gui.chat.messages.adapters.MessageFormAdapter.Item.ItemCheckBox
import ru.usedesk.chat_sdk.entity.UsedeskForm
import ru.usedesk.chat_sdk.entity.UsedeskForm.Field

internal class CheckBoxViewHolder(
    private val binding: MessageFormAdapter.CheckBoxBinding,
    private val onEvent: (Event) -> Unit
) : BaseViewHolder(binding.rootView) {

    private val checkedDrawable = binding.styleValues.getId(R.attr.usedesk_drawable_1)
    private val uncheckedDrawable = binding.styleValues.getId(R.attr.usedesk_drawable_2)
    private val checkedDisabledDrawable = binding.styleValues.getId(R.attr.usedesk_drawable_3)
    private val uncheckedErrorDrawable = binding.styleValues.getId(R.attr.usedesk_drawable_4)
    private val textColorEnabled = binding.styleValues.getColor(R.attr.usedesk_text_color_1)
    private val textColorDisabled = binding.styleValues.getColor(R.attr.usedesk_text_color_2)

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
        val enabled = when (state) {
            UsedeskForm.State.SENDING_FAILED,
            UsedeskForm.State.LOADED -> true
            else -> false
        }
        binding.tvText.run {
            text = Html.fromHtml(
                checkBox.name + when {
                    checkBox.required -> REQUIRED_POSTFIX_HTML
                    else -> ""
                }
            )
            setTextColor(
                when {
                    enabled -> textColorEnabled
                    else -> textColorDisabled
                }
            )
        }
        binding.ivChecked.setImageResource(
            when {
                checkBox.checked && enabled -> checkedDrawable
                checkBox.checked && !enabled -> checkedDisabledDrawable
                checkBox.hasError -> uncheckedErrorDrawable
                else -> uncheckedDrawable
            }
        )
        binding.ivChecked.run {
            isClickable = enabled
            isFocusable = enabled
            if (enabled) {
                setOnClickListener {
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
    }
}