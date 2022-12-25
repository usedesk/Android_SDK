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
import ru.usedesk.chat_gui.chat.messages.adapters.MessageFormsAdapter
import ru.usedesk.chat_gui.chat.messages.adapters.MessageFormsAdapter.Item
import ru.usedesk.chat_sdk.entity.UsedeskForm
import ru.usedesk.chat_sdk.entity.UsedeskMessageAgentText.Field

internal class TextViewHolder(
    private val binding: MessageFormsAdapter.TextBinding,
    private val onEvent: (Event) -> Unit
) : BaseViewHolder(binding.rootView) {

    private var onTextChangedListener: (String) -> Unit = {}

    private val backgroundSelector = binding.styleValues.getId(R.attr.usedesk_drawable_1)
    private val backgroundError = binding.styleValues.getId(R.attr.usedesk_drawable_2)

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
        var text: Field.Text? = null
        var formState: UsedeskForm.State? = null
        stateFlow.onEach { state ->
            val form = state.formMap[messageId]
            if (form != null) {
                val newText = form.fields.first { it.id == item.fieldId } as Field.Text
                val newFormState = form.state
                if (formState != newFormState || text?.hasError != newText.hasError) {
                    text = newText
                    formState = newFormState
                    update(
                        messageId,
                        newText,
                        newFormState
                    )
                }
            }
        }.launchIn(viewHolderScope)
    }

    private fun update(
        messageId: Long,
        text: Field.Text,
        formState: UsedeskForm.State
    ) {
        binding.etText.run {
            isEnabled = when (formState) {
                UsedeskForm.State.SENDING_FAILED,
                UsedeskForm.State.LOADED -> true
                else -> false
            }
            hint = Html.fromHtml(
                text.name + when {
                    text.required -> REQUIRED_POSTFIX_HTML
                    else -> ""
                }
            )
            inputType = InputType.TYPE_CLASS_TEXT or when (text.type) {
                Field.Text.Type.EMAIL -> InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                Field.Text.Type.PHONE -> InputType.TYPE_CLASS_PHONE
                Field.Text.Type.NAME -> InputType.TYPE_TEXT_VARIATION_PERSON_NAME or
                        InputType.TYPE_TEXT_FLAG_CAP_WORDS
                Field.Text.Type.NOTE -> InputType.TYPE_TEXT_FLAG_CAP_SENTENCES or
                        InputType.TYPE_TEXT_FLAG_MULTI_LINE
                Field.Text.Type.POSITION -> InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                Field.Text.Type.NONE -> InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            }
            if ((this.text?.toString() ?: "") != text.text) {
                onTextChangedListener = {}
                setText(text.text)
            }
            setBackgroundResource(
                when {
                    text.hasError -> backgroundError
                    else -> backgroundSelector
                }
            )
            onTextChangedListener = {
                onEvent(
                    Event.FormChanged(
                        messageId,
                        text.copy(text = it)
                    )
                )
            }
        }
    }
}