
package ru.usedesk.common_gui

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

class UsedeskCommonFieldCheckBoxAdapter(
    private val binding: Binding
) {
    private val uncheckedImageId: Int
    private val checkedImageId: Int

    init {
        binding.styleValues.getStyleValues(R.attr.usedesk_common_field_checkbox_image).let {
            uncheckedImageId = it.getId(R.attr.usedesk_drawable_1)
            checkedImageId = it.getId(R.attr.usedesk_drawable_2)
        }
        setChecked(false)
    }

    fun setOnClickListener(onClickListener: () -> Unit) {
        binding.lClickable.setOnClickListener {
            onClickListener()
        }
    }

    fun setChecked(checked: Boolean) {
        binding.ivCheck.setImageResource(
            if (checked) {
                checkedImageId
            } else {
                uncheckedImageId
            }
        )
    }

    fun setTitle(title: String) {
        binding.tvTitle.text = title
    }

    class Binding(rootView: View, defaultStyleId: Int) : UsedeskBinding(rootView, defaultStyleId) {
        val lClickable: ViewGroup = rootView.findViewById(R.id.l_clickable)
        val tvTitle: TextView = rootView.findViewById(R.id.tv_title)
        val ivCheck: ImageView = rootView.findViewById(R.id.iv_check)
    }
}