package ru.usedesk.knowledgebase_gui.screens

import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import android.widget.TextView
import ru.usedesk.common_gui.UsedeskBinding
import ru.usedesk.common_gui.showKeyboard
import ru.usedesk.knowledgebase_gui.R

internal class ToolbarSearchAdapter(
    private val binding: Binding,
    onSearchClick: (String) -> Unit,
    onCancelClick: () -> Unit
) {

    init {
        binding.tvCancel.setOnClickListener {
            onCancelClick()
        }
        binding.etQuery.apply {
            setOnKeyListener { _, keyCode, event ->
                if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                    onSearchClick(text.toString())
                    true
                } else {
                    false
                }
            }
        }
    }

    fun show() {
        binding.rootView.visibility = View.VISIBLE
        showKeyboard(binding.etQuery)
    }

    fun hide() {
        binding.rootView.visibility = View.GONE
    }

    class Binding(
        rootView: View,
        defaultStyleId: Int
    ) : UsedeskBinding(rootView, defaultStyleId) {
        val etQuery: EditText = rootView.findViewById(R.id.et_query)
        val tvCancel: TextView = rootView.findViewById(R.id.tv_cancel)
    }
}