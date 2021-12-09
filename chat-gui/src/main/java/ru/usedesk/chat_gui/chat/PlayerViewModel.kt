package ru.usedesk.chat_gui.chat

import com.google.android.exoplayer2.ExoPlayer
import ru.usedesk.common_gui.UsedeskLiveData
import ru.usedesk.common_gui.UsedeskViewModel

class PlayerViewModel : UsedeskViewModel() {

    val modelLiveData = UsedeskLiveData(Model())

    var exoPlayer: ExoPlayer? = null
    var lastPlaying: Boolean = false

    private fun setModel(onUpdate: (Model) -> Model) {
        modelLiveData.value = onUpdate(modelLiveData.value)
    }

    fun videoApply(videoKey: String, name: String) {
        setModel { model ->
            model.copy(
                mode = Mode.VIDEO_EXO_PLAYER,
                key = videoKey,
                name = name,
                fullscreen = false
            )
        }
    }

    fun audioApply(audioKey: String, name: String) {
        setModel { model ->
            model.copy(
                mode = Mode.AUDIO_EXO_PLAYER,
                key = audioKey,
                name = name,
                fullscreen = false
            )
        }
    }

    fun fullscreen() {
        setModel { model ->
            model.copy(
                fullscreen = if (model.key.isNotEmpty()) {
                    !model.fullscreen
                } else {
                    false
                }
            )
        }
    }

    fun onBackPressed(): Boolean {
        val modelValue = modelLiveData.value
        return if (modelValue.fullscreen || modelValue.fullscreen) {
            setModel { model ->
                model.copy(
                    fullscreen = false
                )
            }
            true
        } else {
            false
        }
    }

    fun reset() {
        setModel { model ->
            model.copy(
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