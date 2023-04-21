
package ru.usedesk.chat_gui.chat.messages.adapters.holders

import android.text.Html
import android.text.InputType
import androidx.core.widget.addTextChangedListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.usedesk.chat_gui.R
import ru.usedesk.chat_gui.chat.messages.MessagesViewModel
import ru.usedesk.chat_gui.chat.messages.MessagesViewModel.Event
import ru.usedesk.chat_gui.chat.messages.adapters.MessageFormAdapter
import ru.usedesk.chat_gui.chat.messages.adapters.MessageFormAdapter.Item
import ru.usedesk.chat_sdk.entity.UsedeskForm
import ru.usedesk.chat_sdk.entity.UsedeskForm.Field

internal class TextViewHolder(
    private val binding: MessageFormAdapter.TextBinding,
    private val onEvent: (Event) -> Unit
) : BaseViewHolder(binding.rootView) {

    private var onTextChangedListener: (String) -> Unit = {}

    private val backgroundSelector = binding.styleValues.getId(R.attr.usedesk_drawable_1)
    private val backgroundError = binding.styleValues.getId(R.attr.usedesk_drawable_2)
    private val textColorEnabled = binding.styleValues.getColor(R.attr.usedesk_text_color_1)
    private val textColorDisabled = binding.styleValues.getColor(R.attr.usedesk_text_color_2)

    init {
        binding.etText.addTextChangedListener {
            onTextChangedListener(it?.toString() ?: "")
        }
    }

    override fun bind(
        messageId: Long,
        item: Item,
        scope: CoroutineScope,
        stateFlow: StateFlow<MessagesViewModel.State>
    ) {
        item as Item.ItemText
        var fieldText: Field.Text = stateFlow.value.formMap[messageId]
            ?.fields
            ?.firstOrNull { it.id == item.fieldId } as? Field.Text
            ?: Field.Text(item.fieldId, "", false, type = Field.Text.Type.NONE)
        var formState: UsedeskForm.State? = null

        binding.etText.run {
            hint = Html.fromHtml(
                fieldText.name + when {
                    fieldText.required -> REQUIRED_POSTFIX_HTML
                    else -> ""
                }
            )
            inputType = InputType.TYPE_CLASS_TEXT or when (fieldText.type) {
                Field.Text.Type.EMAIL -> InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                Field.Text.Type.PHONE -> InputType.TYPE_CLASS_PHONE
                Field.Text.Type.NAME -> InputType.TYPE_TEXT_VARIATION_PERSON_NAME or
                        InputType.TYPE_TEXT_FLAG_CAP_WORDS
                Field.Text.Type.NOTE -> InputType.TYPE_TEXT_FLAG_CAP_SENTENCES or
                        InputType.TYPE_TEXT_FLAG_MULTI_LINE
                Field.Text.Type.POSITION -> InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                Field.Text.Type.NONE -> InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            }
            if ((text?.toString() ?: "") != fieldText.text) {
                onTextChangedListener = {}
                setText(fieldText.text)
            }
            setBackgroundResource(
                when {
                    fieldText.hasError -> backgroundError
                    else -> backgroundSelector
                }
            )
            onTextChangedListener = {
                onEvent(
                    Event.FormChanged(
                        messageId,
                        fieldText.copy(text = it)
                    )
                )
            }
        }

        stateFlow.onEach { state ->
            val form = state.formMap[messageId]
            if (form != null) {
                val newText = form.fields.first { it.id == item.fieldId } as Field.Text
                val newFormState = form.state
                if (formState != newFormState || fieldText.hasError != newText.hasError) {
                    fieldText = newText
                    formState = newFormState
                    update(
                        newText,
                        newFormState
                    )
                }
            }
        }.launchIn(viewHolderScope)
    }

    private fun update(
        fieldText: Field.Text,
        formState: UsedeskForm.State
    ) {
        binding.etText.run {
            isEnabled = when (formState) {
                UsedeskForm.State.SENDING_FAILED,
                UsedeskForm.State.LOADED -> {
                    setTextColor(textColorEnabled)
                    true
                }
                else -> {
                    setTextColor(textColorDisabled)
                    clearFocus()
                    false
                }
            }
            setBackgroundResource(
                when {
                    fieldText.hasError -> backgroundError
                    else -> backgroundSelector
                }
            )
        }
    }
}