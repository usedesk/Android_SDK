package ru.usedesk.common_gui.external

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import ru.usedesk.common_gui.databinding.UsedeskViewToolbarBinding

class UsedeskToolbar(
        activity: AppCompatActivity,
        private val toolbarBinding: UsedeskViewToolbarBinding
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

    fun invalidate() {
        //TODO: добавить пересчёт ширины заголовка
    }
}