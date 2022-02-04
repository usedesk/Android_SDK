package ru.usedesk.common_gui

import android.view.View

open class UsedeskBinding(
    val rootView: View,
    val styleValues: UsedeskResourceManager.StyleValues
) {
    constructor(
        rootView: View,
        defaultStyleId: Int
    ) : this(
        rootView,
        UsedeskResourceManager.getStyleValues(rootView.context, defaultStyleId)
    )
}