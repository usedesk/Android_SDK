package ru.usedesk.common_gui

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class UsedeskToolbar(
        activity: AppCompatActivity,
        private val toolbarBinding: Binding
) {

    init {
        activity.setSupportActionBar(toolbarBinding.toolbar)
    }

    fun setTitle(title: String) {
        toolbarBinding.tvTitle.text = title
    }

    fun setTitle(titleId: Int) {
        toolbarBinding.tvTitle.setText(titleId)
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

    class Binding(
            val rootView: ViewGroup
    ) {
        val toolbar: Toolbar = rootView.findViewById(R.id.toolbar)
        val ivBack: ImageView = rootView.findViewById(R.id.iv_back)
        val tvTitle: TextView = rootView.findViewById(R.id.tv_title)
        val lAction: ViewGroup = rootView.findViewById(R.id.l_action)
        val ivAction: ImageView = rootView.findViewById(R.id.iv_action)
    }
}