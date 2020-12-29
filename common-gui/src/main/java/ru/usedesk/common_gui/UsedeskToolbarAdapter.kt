package ru.usedesk.common_gui

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class UsedeskToolbarAdapter(
        activity: AppCompatActivity,
        private val toolbarBinding: Binding
) {

    init {
        activity.setSupportActionBar(toolbarBinding.toolbar)
    }

    fun setTitle(title: String) {
        toolbarBinding.tvTitle.text = title
    }

    fun setBackButton(onBackPressed: () -> Unit) {
        toolbarBinding.ivBack.apply {
            setOnClickListener {
                onBackPressed()
            }
            visibility = View.VISIBLE
        }
    }

    fun setActionButton(imageId: Int, onActionClick: () -> Unit) {
        toolbarBinding.ivAction.apply {
            setImageResource(imageId)
            setOnClickListener {
                onActionClick.invoke()
            }
            visibility = View.VISIBLE
        }
    }

    class Binding(rootView: View, defaultStyleId: Int) : UsedeskBinding(rootView, defaultStyleId) {
        val toolbar: Toolbar = rootView as Toolbar
        val ivBack: ImageView = rootView.findViewById(R.id.iv_back)
        val tvTitle: TextView = rootView.findViewById(R.id.tv_title)
        val ivAction: ImageView = rootView.findViewById(R.id.iv_action)
    }
}