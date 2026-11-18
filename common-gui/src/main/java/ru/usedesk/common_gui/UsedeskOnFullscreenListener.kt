package ru.usedesk.common_gui

import android.view.ViewGroup

interface UsedeskOnFullscreenListener {
    fun getFullscreenLayout(): ViewGroup

    fun onFullscreenChanged(fullscreen: Boolean)
}
