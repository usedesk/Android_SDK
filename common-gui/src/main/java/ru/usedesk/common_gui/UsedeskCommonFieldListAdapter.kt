package ru.usedesk.common_gui

import android.text.Html
import android.view.View
import android.widget.TextView

class UsedeskCommonFieldListAdapter(
        private val binding: Binding,
        onClickListener: () -> Unit
) {
    private val colorTitle = binding.styleValues.getColor(R.attr.usedesk_text_color_1)
    private val colorRequired = binding.styleValues.getColor(R.attr.usedesk_text_color_2)

    init {
        binding.rootView.setOnClickListener {
            onClickListener()
        }
    }

    fun setTitle(title: String, required: Boolean = false) {
        val tail = if (required) {
            String.format("<font color=#%s> *</font>", colorRequired.toString(16))
        } else {
            ""
        }

        val htmlTitle = String.format("<font color=#%s>%s</font>", colorTitle.toString(16), title) + tail
        binding.tvTitle.text = Html.fromHtml(htmlTitle)
    }

    class Binding(rootView: View, defaultStyleId: Int) : UsedeskBinding(rootView, defaultStyleId) {
        val tvTitle: TextView = rootView.findViewById(R.id.tv_title)
    }
}