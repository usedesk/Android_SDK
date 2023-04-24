
package ru.usedesk.chat_gui.chat

import com.google.android.exoplayer2.ExoPlayer
import ru.usedesk.common_gui.UsedeskViewModel

internal class PlayerViewModel : UsedeskViewModel<PlayerViewModel.Model>(Model()) {

    var exoPlayer: ExoPlayer? = null
    var lastPlaying: Boolean = false

    fun videoApply(videoKey: String, name: String) {
        setModel {
            copy(
                mode = Mode.VIDEO_EXO_PLAYER,
                key = videoKey,
                name = name,
                fullscreen = false
            )
        }
    }

    fun audioApply(audioKey: String, name: String) {
        setModel {
            copy(
                mode = Mode.AUDIO_EXO_PLAYER,
                key = audioKey,
                name = name,
                fullscreen = false
            )
        }
    }

    fun fullscreen() {
        setModel {
            copy(
                fullscreen = when {
                    key.isNotEmpty() -> !fullscreen
                    else -> false
                }
            )
        }
    }

    fun onBackPressed() = when {
        modelFlow.value.fullscreen -> {
            setModel { copy(fullscreen = false) }
            true
        }
        else -> false
    }

    fun reset() {
        setModel {
            copy(
                mode = Mode.NONE,
                key = "",
                name = "",
                fullscreen = false
            )
        }
    }

    override fun onCleared() {
        super.onCleared()

        exoPlayer?.release()
        exoPlayer = null
    }

    data class Model(
        val mode: Mode = Mode.NONE,
        val fullscreen: Boolean = false,
        val key: String = "",
        val name: String = ""
    )

    enum class Mode {
        NONE,
        VIDEO_EXO_PLAYER,
        AUDIO_EXO_PLAYER
    }
}