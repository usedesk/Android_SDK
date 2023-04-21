
package ru.usedesk.common_gui

import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

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

fun <BINDING> inflateItem(
    container: ViewGroup,
    defaultLayoutId: Int,
    defaultStyleId: Int,
    createBinding: (View, Int) -> BINDING
): BINDING = inflateItem(
    LayoutInflater.from(container.context),
    container,
    defaultLayoutId,
    defaultStyleId,
    createBinding
)

fun <BINDING> inflateItem(
    inflater: LayoutInflater,
    container: ViewGroup?,
    defaultLayoutId: Int,
    defaultStyleId: Int,
    createBinding: (View, Int) -> BINDING
): BINDING {
    val customStyleId = UsedeskResourceManager.getResourceId(defaultStyleId)
    val wrapper = ContextThemeWrapper(inflater.context, customStyleId)
    val localInflater = inflater.cloneInContext(wrapper)
    val layoutId = UsedeskResourceManager.getResourceId(defaultLayoutId)
    val view = localInflater.inflate(layoutId, container, false)
    return createBinding(view, defaultStyleId)
}