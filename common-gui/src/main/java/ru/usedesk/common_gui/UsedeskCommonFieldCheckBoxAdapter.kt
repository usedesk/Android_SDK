package ru.usedesk.common_gui


import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import ru.usedesk.common_gui.R as commonR

class UsedeskCommonFieldCheckBoxAdapter(
    private val binding: Binding
) {
    private val uncheckedImageId: Int
    private val checkedImageId: Int

    init {
        binding.styleValues.getStyleValues(commonR.attr.usedesk_common_field_checkbox_image).let {
            uncheckedImageId = it.getId(commonR.attr.usedesk_drawable_1)
            checkedImageId = it.getId(commonR.attr.usedesk_drawable_2)
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
        val lClickable: ViewGroup = rootView.findViewById(commonR.id.l_clickable)
        val tvTitle: TextView = rootView.findViewById(commonR.id.tv_title)
        val ivCheck: ImageView = rootView.findViewById(commonR.id.iv_check)
    }
}