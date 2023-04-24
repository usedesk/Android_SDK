
package ru.usedesk.chat_gui.showfile

import ru.usedesk.chat_sdk.entity.UsedeskFile
import ru.usedesk.common_gui.UsedeskViewModel

internal class ShowFileViewModel : UsedeskViewModel<ShowFileViewModel.Model>(Model()) {

    fun setFile(file: UsedeskFile) {
        setModel { copy(file = file) }
    }

    fun onLoaded(success: Boolean) {
        setModel { copy(error = !success) }
    }

    fun onImageClick() {
        setModel { copy(panelShow = !panelShow) }
    }

    fun onRetryPreview() {
        setModel { copy(error = false) }
    }

    data class Model(
        val file: UsedeskFile? = null,
        val error: Boolean = false,
        val panelShow: Boolean = true
    )
}