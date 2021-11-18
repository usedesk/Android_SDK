package ru.usedesk.chat_gui.chat

import android.view.ViewGroup

interface IUsedeskMediaPlayerAdapter {

    fun onBackPressed(): Boolean

    fun applyPlayer(
        lMinimized: ViewGroup,
        mediaKey: String,
        playerType: PlayerType,
        doOnApply: () -> Unit,
        doOnCancelPlay: () -> Unit,
        doOnControlsVisibilityChanged: ((Boolean) -> Unit) = {}
    )

    fun cancelPlayer(key: String)

    /**
     * Вызывается для того, чтобы прикрепиться к адаптеру, для реакции на minimize или переключения
     * плеера
     */
    fun reapplyPlayer(
        lVideoMinimized: ViewGroup,
        mediaKey: String,
        doOnReapply: () -> Unit,
        doOnCancelPlay: () -> Unit,
        doOnControlsVisibilityChanged: ((Boolean) -> Unit) = {}
    )

    enum class PlayerType {
        //YOUTUBE,
        VIDEO,
        AUDIO
    }
}