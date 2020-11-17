package ru.usedesk.chat_gui.internal._extra

import androidx.fragment.app.Fragment

abstract class UsedeskFragment : Fragment() {
    private var inited = false

    protected fun doInit(init: () -> Unit) {
        if (!inited) {
            inited = true
            init()
        }
    }

    open fun onBackPressed(): Boolean = false
}