package ru.usedesk.common_gui

import android.view.View

open class UsedeskBinding(
        val rootView: View,
        defaultStyleId: Int
) {
    val styleValues = UsedeskResourceManager.getStyleValues(rootView.context, defaultStyleId)
}