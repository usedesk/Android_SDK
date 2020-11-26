package ru.usedesk.common_gui.internal

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