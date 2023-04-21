
package ru.usedesk.common_gui

import android.text.Html
import android.view.View
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class UsedeskCommonFieldTextAdapter(val binding: Binding) {

    private val colorTitle = binding.styleValues.getColor(R.attr.usedesk_text_color_1)
    private val colorRequired = binding.styleValues.getColor(R.attr.usedesk_text_color_2)

    fun setTitle(title: String, required: Boolean = false) {
        val tail = when {
            required -> String.format("<font color=#%s> *</font>", colorRequired.toString(16))
            else -> ""
        }

        val htmlTitle =
            String.format("<font color=#%s>%s</font>", colorTitle.toString(16), title) + tail
        binding.tilTitle.hint = Html.fromHtml(htmlTitle)
    }

    fun setText(text: String?) {
        binding.etText.setText(text)
    }

    fun setTextChangeListener(onChanged: (String) -> Unit) {
        binding.etText.addTextChangedListener(UsedeskTextChangeListener(onChanged))
    }

    fun showError(error: String?) {
        binding.tilTitle.error = error
    }

    fun show(show: Boolean) {
        binding.rootView.visibility = visibleGone(show)
    }

    class Binding(rootView: View, defaultStyleId: Int) : UsedeskBinding(rootView, defaultStyleId) {
        val tilTitle: TextInputLayout = rootView.findViewById(R.id.til_title)
        val etText: TextInputEditText = rootView.findViewById(R.id.et_text)
    }
}