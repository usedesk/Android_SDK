package ru.usedesk.chat_gui.chat

import ru.usedesk.common_gui.UsedeskLiveData
import ru.usedesk.common_gui.UsedeskViewModel

class PlayerViewModel : UsedeskViewModel() {

    val modelLiveData = UsedeskLiveData(Model())

    private fun setModel(onUpdate: (Model) -> Model) {
        modelLiveData.value = onUpdate(modelLiveData.value)
    }

    fun videoApply(videoKey: String) {
        setModel { model ->
            model.copy(
                mode = Mode.VIDEO_EXO_PLAYER,
                key = videoKey,
                fullscreen = false
            )
        }
    }

    fun audioApply(audioKey: String) {
        setModel { model ->
            model.copy(
                mode = Mode.AUDIO_EXO_PLAYER,
                key = audioKey,
                fullscreen = false
            )
        }
    }

    /*fun youTubeApply(youTubeKey: String) {
        setModel { model ->
            model.copy(
                mode = Mode.YOU_TUBE_PLAYER,
                key = youTubeKey,
                fullscreen = false
            )
        }
    }*/

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
                fullscreen = false
            )
        }
    }

    data class Model(
        val mode: Mode = Mode.NONE,
        val fullscreen: Boolean = false,
        val key: String = ""
    )

    enum class Mode {
        NONE,

        //YOU_TUBE_PLAYER,
        VIDEO_EXO_PLAYER,
        AUDIO_EXO_PLAYER
    }
}