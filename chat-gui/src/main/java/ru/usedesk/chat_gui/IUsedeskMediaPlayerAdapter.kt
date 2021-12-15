package ru.usedesk.chat_gui

import android.view.ViewGroup

interface IUsedeskMediaPlayerAdapter {

    /**
     * @return true if event was handled
     */
    fun onBackPressed(): Boolean

    /**
     * Resets the current playback and switches the player's view to lMinimized.
     *
     * @param lMinimized Parent view for minimized player
     * @param mediaKey URL of the video/audio source
     * @param mediaName Video/audio name
     * @param playerType Video/audio type
     * @param onCancel Called when the player cancels playing the current media
     * @param onControlsHeightChanged Called when player controls are visible
     */
    fun attachPlayer(
        lMinimized: ViewGroup,
        mediaKey: String,
        mediaName: String,
        playerType: PlayerType,
        onCancel: () -> Unit,
        onControlsHeightChanged: ((Int) -> Unit) = {}
    )

    /**
     * Places player view inside lMinimized if mediaKey is equal to the mediaKey of current player.
     *
     * @param lMinimized Parent view for minimized player
     * @param mediaKey URL of the video/audio source
     * @param onCancel Called when the player cancels playing the current media
     * @param onControlsHeightChanged Called when player controls are visible
     *
     * @return true if player was reattached
     */
    fun reattachPlayer(
        lMinimized: ViewGroup,
        mediaKey: String,
        onCancel: () -> Unit,
        onControlsHeightChanged: ((Int) -> Unit) = {}
    ): Boolean

    /**
     * Detach current player view if mediaKey is equal to the mediaKey of current player.
     */
    fun detachPlayer(key: String): Boolean

    enum class PlayerType {
        VIDEO,
        AUDIO
    }
}