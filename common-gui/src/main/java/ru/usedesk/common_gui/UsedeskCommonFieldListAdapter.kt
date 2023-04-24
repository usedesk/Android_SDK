
package ru.usedesk.common_gui

import android.text.Html
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class UsedeskCommonFieldListAdapter(
    private val binding: Binding
) {
    private val colorTitle = binding.styleValues.getColor(R.attr.usedesk_text_color_1)
    private val colorRequired = binding.styleValues.getColor(R.attr.usedesk_text_color_2)

    init {
        setTitle("")
        setText("")
    }

    fun setOnClickListener(onClickListener: () -> Unit) {
        binding.lClickable.setOnClickListener {
            onClickListener()
        }
    }

    fun setTitle(title: String, required: Boolean = false) {
        val tail = if (required) {
            String.format("<font color=#%s> *</font>", colorRequired.toString(16))
        } else {
            ""
        }

        val htmlTitle =
            String.format("<font color=#%s>%s</font>", colorTitle.toString(16), title) + tail
        binding.tvTitle.text = Html.fromHtml(htmlTitle)
    }

    fun setText(text: String?) {
        binding.tvValue.text = text
        binding.tvValue.visibility = visibleGone(text?.isEmpty() == false)
    }

    class Binding(rootView: View, defaultStyleId: Int) : UsedeskBinding(rootView, defaultStyleId) {
        val lClickable: ViewGroup = rootView.findViewById(R.id.l_clickable)
        val tvTitle: TextView = rootView.findViewById(R.id.tv_title)
        val tvValue: TextView = rootView.findViewById(R.id.tv_value)
    }
}