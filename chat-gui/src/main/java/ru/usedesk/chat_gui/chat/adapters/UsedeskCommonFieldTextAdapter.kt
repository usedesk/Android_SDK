package ru.usedesk.chat_gui.chat.adapters

import android.view.View
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import ru.usedesk.chat_gui.R
import ru.usedesk.common_gui.TextChangeListener
import ru.usedesk.common_gui.UsedeskBinding
import ru.usedesk.common_gui.showKeyboard
import ru.usedesk.common_gui.visibleGone

class UsedeskCommonFieldTextAdapter(
        private val binding: Binding
) {

    init {
        binding.tilTitle.hint = binding.styleValues.getString(R.attr.usedesk_text_1)
    }

    fun setText(text: String?) {
        binding.etText.setText(text)
    }

    fun setTextChangeListener(onChanged: (String) -> Unit) {
        binding.etText.addTextChangedListener(TextChangeListener {
            onChanged(it)
        })
    }

    fun showError(show: Boolean) {
        binding.tilTitle.error = if (show) {
            showKeyboard(binding.etText)
            binding.styleValues.getString(R.attr.usedesk_text_2)
        } else {
            null
        }
    }

    fun show(show: Boolean) {
        binding.rootView.visibility = visibleGone(show)
    }

    fun getText(): String = binding.etText.text?.toString() ?: ""

    class Binding(rootView: View, defaultStyleId: Int) : UsedeskBinding(rootView, defaultStyleId) {
        val tilTitle: TextInputLayout = rootView.findViewById(R.id.til_title)
        val etText: TextInputEditText = rootView.findViewById(R.id.et_text)
    }
}