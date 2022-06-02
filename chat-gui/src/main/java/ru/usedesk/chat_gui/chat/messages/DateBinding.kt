package ru.usedesk.chat_gui.chat.messages

import android.view.View
import android.widget.TextView
import ru.usedesk.chat_gui.R
import ru.usedesk.common_gui.UsedeskBinding

internal class DateBinding(rootView: View, defaultStyleId: Int) :
    UsedeskBinding(rootView, defaultStyleId) {
    val tvDate: TextView = rootView.findViewById(R.id.tv_date)

    val defaultTopMargin = styleValues.getStyleValues(
        R.attr.usedesk_chat_message_date_text
    ).getPixels(android.R.attr.layout_marginTop).toInt()
}