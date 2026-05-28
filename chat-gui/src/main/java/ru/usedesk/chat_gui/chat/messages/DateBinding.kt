package ru.usedesk.chat_gui.chat.messages

import android.view.View
import android.widget.TextView
import ru.usedesk.common_gui.UsedeskBinding
import ru.usedesk.chat_gui.R as chatR

internal class DateBinding(rootView: View, defaultStyleId: Int) :
    UsedeskBinding(rootView, defaultStyleId) {
    val tvDate: TextView = rootView.findViewById(chatR.id.tv_date)
}