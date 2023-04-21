
package ru.usedesk.chat_gui

import android.view.ViewGroup

interface IUsedeskOnFullscreenListener {
    /**
     * Returns a container for a fullscreen video player.
     */
    fun getFullscreenLayout(): ViewGroup

    /**
     * Fullscreen mode switch callback.
     */
    fun onFullscreenChanged(fullscreen: Boolean)
}