package ru.usedesk.chat_gui.showfile

import ru.usedesk.chat_sdk.entity.UsedeskFile
import ru.usedesk.common_gui.UsedeskViewModel

internal class ShowFileViewModel : UsedeskViewModel<ShowFileViewModel.Model>(Model()) {

    fun init(file: UsedeskFile) {
        doInit {
            setModel { model ->
                model.copy(file = file)
            }
        }
    }

    fun onLoaded(success: Boolean) {
        setModel { model ->
            model.copy(error = !success)
        }
    }

    fun onImageClick() {
        setModel { model ->
            model.copy(panelShow = !model.panelShow)
        }
    }

    fun onRetryPreview() {
        setModel { model ->
            model.copy(error = false)
        }
    }

    data class Model(
        val file: UsedeskFile? = null,
        val error: Boolean = false,
        val panelShow: Boolean = true
    )
}