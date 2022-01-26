package ru.usedesk.chat_gui.showfile

import ru.usedesk.chat_sdk.entity.UsedeskFile
import ru.usedesk.common_gui.UsedeskLiveData
import ru.usedesk.common_gui.UsedeskViewModel

internal class ShowFileViewModel : UsedeskViewModel() {

    val modelLiveData = UsedeskLiveData(Model())

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

    private fun setModel(onUpdate: (Model) -> Model) {
        modelLiveData.value = onUpdate(modelLiveData.value)
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