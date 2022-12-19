package ru.usedesk.chat_gui.chat.messages.adapters.holders

import android.text.InputType
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
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

    init {
        binding.etText.addTextChangedListener {
            onTextChangedListener(it?.toString() ?: "")
        }
    }

    private fun EditText.postScrollTo(scrollAgain: Boolean) {
        binding.rootView.postDelayed({
            if (isFocused) {
                val start = selectionStart
                val end = selectionEnd
                text = text
                setSelection(start, end)
                if (scrollAgain) {
                    postScrollTo(false)
                }
            }
        }, 500)
    }

    override fun bind(
        messageId: Long,
        item: Item,
        state: UsedeskForm.State
    ) {
        item as Item.ItemText
        binding.etText.run {
            clearFocus()
            hint = item.text.name
            onTextChangedListener = {}
            setText(item.text.text)
            setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    postScrollTo(true)
                }
            }
            when (item.text.type) {
                Field.Text.Type.EMAIL -> {
                    inputType = InputType.TYPE_CLASS_TEXT or
                            InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                    maxLines = 1
                    setOnTouchListener { _, _ -> false }
                }
                Field.Text.Type.PHONE -> {
                    inputType = InputType.TYPE_CLASS_TEXT or
                            InputType.TYPE_CLASS_PHONE
                    maxLines = 1
                    setOnTouchListener { _, _ -> false }
                }
                Field.Text.Type.NAME -> {
                    inputType = InputType.TYPE_CLASS_TEXT or
                            InputType.TYPE_TEXT_VARIATION_PERSON_NAME or
                            InputType.TYPE_TEXT_FLAG_CAP_WORDS
                    maxLines = 1
                    setOnTouchListener { _, _ -> false }
                }
                Field.Text.Type.NOTE -> {
                    inputType = InputType.TYPE_CLASS_TEXT or
                            InputType.TYPE_TEXT_FLAG_CAP_SENTENCES or
                            InputType.TYPE_TEXT_FLAG_MULTI_LINE
                    maxLines = 3
                    setOnTouchListener { v, _ ->
                        v.parent.requestDisallowInterceptTouchEvent(true)
                        false
                    }
                }
                Field.Text.Type.POSITION -> {
                    inputType = InputType.TYPE_CLASS_TEXT or
                            InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                    maxLines = 1
                    setOnTouchListener { _, _ -> false }
                }
            }
            onTextChangedListener = {
                onEvent(
                    Event.FormChanged(
                        messageId,
                        item.text.copy(text = it)
                    )
                )
            }
        }
    }
}