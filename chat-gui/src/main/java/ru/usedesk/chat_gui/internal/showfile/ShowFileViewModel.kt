package ru.usedesk.chat_gui.internal.showfile

import androidx.lifecycle.MutableLiveData
import ru.usedesk.chat_sdk.internal.domain.entity.UsedeskFile
import ru.usedesk.common_gui.internal.UsedeskViewModel

class ShowFileViewModel : UsedeskViewModel() {

    val fileUrlLiveData = MutableLiveData<UsedeskFile>()
    val errorLiveData = MutableLiveData(false)
    val panelShowLiveData = MutableLiveData(true)

    fun init(usedeskFile: UsedeskFile?) {
        doInit {
            fileUrlLiveData.value = usedeskFile
        }
    }

    fun onLoaded(success: Boolean) {
        if (success) {
            errorLiveData.value = false
        } else {
            fileUrlLiveData.value = null
            errorLiveData.value = true
        }
    }

    fun onImageClick() {
        panelShowLiveData.value = panelShowLiveData.value != true
    }
}