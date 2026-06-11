package ru.usedesk.common_gui

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import ru.usedesk.common_gui.R as commonR

class UsedeskToolbarAdapter(
    private val binding: Binding,
    supportWindowInsets: Boolean,
) {

    init {
        if (supportWindowInsets) {
            binding.rootView.insetsAsPaddings(ignoreNavigationBar = true, ignoreIme = true)
        }
    }

    fun setTitle(title: String?) {
        binding.tvTitle.text = title
    }

    fun setBackButton(onBackPressed: () -> Unit) {
        binding.ivBack.apply {
            setOnClickListener {
                onBackPressed()
            }
            visibility = View.VISIBLE
        }
    }

    fun setActionButton(onActionClick: () -> Unit) {
        binding.ivAction.apply {
            setOnClickListener {
                onActionClick.invoke()
            }
            visibility = View.VISIBLE
        }
    }

    fun show() {
        binding.rootView.visibility = View.VISIBLE
        hideKeyboard(binding.rootView)
    }

    fun hide() {
        binding.rootView.visibility = View.GONE
    }

    class Binding(rootView: View, defaultStyleId: Int) : UsedeskBinding(rootView, defaultStyleId) {
        val toolbar: Toolbar = rootView as Toolbar
        val ivBack: ImageView = rootView.findViewById(commonR.id.iv_back)
        val tvTitle: TextView = rootView.findViewById(commonR.id.tv_title)
        val ivAction: ImageView = rootView.findViewById(commonR.id.iv_action)
    }
}