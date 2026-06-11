package ru.usedesk.common_gui

import android.view.ViewGroup

interface UsedeskOnFullscreenListener {
    /**
     * Returns a container for a fullscreen video player.
     */
    fun getFullscreenLayout(): ViewGroup

    /**
     * Fullscreen mode switch callback.
     */
    fun onFullscreenChanged(fullscreen: Boolean)
}
