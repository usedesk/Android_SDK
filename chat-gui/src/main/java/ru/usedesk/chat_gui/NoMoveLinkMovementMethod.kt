package ru.usedesk.chat_gui

import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.view.MotionEvent
import android.widget.TextView

internal object NoMoveLinkMovementMethod : LinkMovementMethod() {
    override fun onTouchEvent(
        widget: TextView?,
        buffer: Spannable?,
        event: MotionEvent?
    ): Boolean = when (event?.action) {
        MotionEvent.ACTION_DOWN -> false
        else -> super.onTouchEvent(widget, buffer, event)
    }
}