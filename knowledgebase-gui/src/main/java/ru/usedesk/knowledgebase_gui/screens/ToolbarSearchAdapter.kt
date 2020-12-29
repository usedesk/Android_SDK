package ru.usedesk.knowledgebase_gui.screens

import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import ru.usedesk.common_gui.UsedeskBinding
import ru.usedesk.knowledgebase_gui.R

class ToolbarSearchAdapter {

    class Binding(rootView: View,
                  defaultStyleId: Int) : UsedeskBinding(rootView, defaultStyleId) {
        val toolbar: Toolbar = rootView.findViewById(R.id.toolbar_search)
        val etQuery: EditText = rootView.findViewById(R.id.et_query)
        val tvCancel: TextView = rootView.findViewById(R.id.tv_cancel)
    }
}