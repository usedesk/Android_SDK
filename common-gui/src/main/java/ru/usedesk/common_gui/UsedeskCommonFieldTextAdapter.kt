package ru.usedesk.common_gui

import android.text.Html
import android.view.View
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class UsedeskCommonFieldTextAdapter(
        val binding: Binding
) {

    private val title = binding.styleValues.getString(R.attr.usedesk_text_1)
    private val error = binding.styleValues.getString(R.attr.usedesk_text_2)
    private val colorTitle = binding.styleValues.getColor(R.attr.usedesk_text_color_1)
    private val colorRequired = binding.styleValues.getColor(R.attr.usedesk_text_color_2)

    init {
        setTitle(title)
    }

    fun setTitle(title: String, required: Boolean = false) {
        val tail = if (required) {
            String.format("<font color=#%s> *</font>", colorRequired.toString(16))
        } else {
            ""
        }

        val htmlTitle = String.format("<font color=#%s>%s</font>", colorTitle.toString(16), title) + tail
        binding.tilTitle.hint = Html.fromHtml(htmlTitle)
    }

    fun setText(text: String?) {
        binding.etText.setText(text)
    }

    fun getText(): String = binding.etText.text?.toString() ?: ""

    fun setTextChangeListener(onChanged: (String) -> Unit) {
        binding.etText.addTextChangedListener(TextChangeListener {
            onChanged(it)
        })
    }

    fun showError(show: Boolean) {
        binding.tilTitle.error = if (show) {
            showKeyboard(binding.etText)
            error
        } else {
            null
        }
    }

    fun show(show: Boolean) {
        binding.rootView.visibility = visibleGone(show)
    }

    class Binding(rootView: View, defaultStyleId: Int) : UsedeskBinding(rootView, defaultStyleId) {
        val tilTitle: TextInputLayout = rootView.findViewById(R.id.til_title)
        val etText: TextInputEditText = rootView.findViewById(R.id.et_text)
    }
}