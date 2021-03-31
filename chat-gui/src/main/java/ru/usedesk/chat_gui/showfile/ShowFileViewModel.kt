package ru.usedesk.chat_gui.showfile

import androidx.lifecycle.MutableLiveData
import ru.usedesk.chat_sdk.entity.UsedeskFile
import ru.usedesk.common_gui.UsedeskViewModel
import ru.usedesk.common_sdk.entity.UsedeskSingleLifeEvent

internal class ShowFileViewModel : UsedeskViewModel() {

    val fileLiveData = MutableLiveData<UsedeskSingleLifeEvent<UsedeskFile>?>()
    val errorLiveData = MutableLiveData<Boolean?>(false)
    val panelShowLiveData = MutableLiveData<Boolean?>(true)

    fun init(file: UsedeskFile?) {
        doInit {
            doFileLoading(file)
        }
    }

    private fun doFileLoading(file: UsedeskFile?) {
        if (file != null) {
            fileLiveData.value = UsedeskSingleLifeEvent(file)
        }
    }

    fun onLoaded(success: Boolean) {
        errorLiveData.value = !success
    }

    fun onImageClick() {
        panelShowLiveData.value = panelShowLiveData.value != true
    }

    fun onRetryPreview() {
        doFileLoading(fileLiveData.value?.data)
    }
}