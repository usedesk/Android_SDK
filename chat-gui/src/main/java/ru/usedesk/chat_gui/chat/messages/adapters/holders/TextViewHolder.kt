package ru.usedesk.chat_gui.chat.messages.adapters.holders

import android.text.InputType
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import ru.usedesk.chat_gui.chat.messages.MessagesViewModel.Event
import ru.usedesk.chat_gui.chat.messages.MessagesViewModel.FormState
import ru.usedesk.chat_gui.chat.messages.adapters.MessageItemsAdapter
import ru.usedesk.chat_sdk.entity.UsedeskMessageAgentText.Form

internal class TextViewHolder(
    private val binding: MessageItemsAdapter.TextBinding,
    private val onEvent: (Event) -> Unit
) : BaseViewHolder<Form.Field.Text, FormState.Text>(binding.rootView) {

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
        form: Form.Field.Text,
        formState: FormState.Text
    ) {
        binding.etText.run {
            hint = form.name
            onTextChangedListener = {}
            setText(formState.text)
            setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    postScrollTo(true)
                }
            }
            when (form.type) {
                Form.Field.Text.Type.EMAIL -> {
                    inputType = InputType.TYPE_CLASS_TEXT or
                            InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                    maxLines = 1
                    setOnTouchListener { _, _ -> false }
                }
                Form.Field.Text.Type.PHONE -> {
                    inputType = InputType.TYPE_CLASS_TEXT or
                            InputType.TYPE_CLASS_PHONE
                    maxLines = 1
                    setOnTouchListener { _, _ -> false }
                }
                Form.Field.Text.Type.NAME -> {
                    inputType = InputType.TYPE_CLASS_TEXT or
                            InputType.TYPE_TEXT_VARIATION_PERSON_NAME or
                            InputType.TYPE_TEXT_FLAG_CAP_WORDS
                    maxLines = 1
                    setOnTouchListener { _, _ -> false }
                }
                Form.Field.Text.Type.NOTE -> {
                    inputType = InputType.TYPE_CLASS_TEXT or
                            InputType.TYPE_TEXT_FLAG_CAP_SENTENCES or
                            InputType.TYPE_TEXT_FLAG_MULTI_LINE
                    maxLines = 3
                    setOnTouchListener { v, _ ->
                        v.parent.requestDisallowInterceptTouchEvent(true)
                        false
                    }
                }
                Form.Field.Text.Type.POSITION -> {
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
                        form,
                        formState.copy(text = it)
                    )
                )
            }
        }
    }
}