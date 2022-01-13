package ru.usedesk.chat_gui.chat.messages

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import ru.usedesk.chat_sdk.UsedeskChatSdk
import ru.usedesk.chat_sdk.domain.IUsedeskChat
import ru.usedesk.chat_sdk.entity.*
import ru.usedesk.common_gui.UsedeskLiveData
import ru.usedesk.common_gui.UsedeskViewModel

internal class MessagesViewModel : UsedeskViewModel() {

    val modelLiveData = UsedeskLiveData(Model())

    private val actionListenerRx: IUsedeskActionListenerRx
    private val usedeskChat: IUsedeskChat = UsedeskChatSdk.requireInstance()

    val configuration = UsedeskChatSdk.requireConfiguration()

    private var messages: List<UsedeskMessage> = listOf()

    private var audioDisposable: Disposable? = null

    init {
        setModel { model ->
            model.copy(messageDraft = usedeskChat.getMessageDraft())
        }

        actionListenerRx = object : IUsedeskActionListenerRx() {
            override fun onMessagesObservable(
                messagesObservable: Observable<List<UsedeskMessage>>
            ): Disposable? {
                return messagesObservable.observeOn(AndroidSchedulers.mainThread()).subscribe {
                    messages = it
                    setModel { model ->//TODO: ATTENTION!!! MAY BE NON-MAIN THREAD
                        model.copy(messages = messages)
                    }
                }
            }
        }
        usedeskChat.addActionListener(actionListenerRx)
    }

    private fun setModel(onUpdate: (Model) -> Model) {
        modelLiveData.value = onUpdate(modelLiveData.value)
    }

    fun onMessageChanged(message: String) {
        setModel { model ->
            model.copy(messageDraft = model.messageDraft.copy(text = message))
        }
        doIt(usedeskChat.setMessageDraftRx(modelLiveData.value.messageDraft))
    }

    fun sendFeedback(message: UsedeskMessageAgentText, feedback: UsedeskFeedback) {
        doIt(usedeskChat.sendRx(message, feedback))
    }

    fun detachFile(file: UsedeskFileInfo) {
        setModel { model ->
            model.copy(
                messageDraft = model.messageDraft.copy(
                    files = model.messageDraft.files - file
                )
            )
        }
        doIt(usedeskChat.setMessageDraftRx(modelLiveData.value.messageDraft))
    }

    fun onSendButton(message: String) {
        doIt(usedeskChat.sendRx(message))
    }

    fun onSend() {
        doIt(usedeskChat.sendMessageDraftRx())

        setModel { model ->
            model.copy(messageDraft = UsedeskMessageDraft())
        }
    }

    fun sendAgain(id: Long) {
        doIt(usedeskChat.sendAgainRx(id))
    }

    fun removeMessage(id: Long) {
        doIt(usedeskChat.removeMessageRx(id))
    }

    fun showToBottomButton(show: Boolean) {
        setModel { model ->
            model.copy(fabToBottom = show)
        }
    }

    fun showAttachmentPanel(show: Boolean) {
        setModel { model ->
            model.copy(
                attachmentPanelVisible = show
            )
        }
    }

    fun fromCamera() {
        setModel { model ->
            model.copy(
                action = Action.FROM_CAMERA_PERMISSION,
                attachmentPanelVisible = false,
                cameraUri = null
            )
        }
    }

    fun fromCameraAvailable() {
        setModel { model ->
            model.copy(
                action = Action.FROM_CAMERA,
                cameraUri = "camera_${System.currentTimeMillis()}.jpg"
            )
        }
    }

    fun fromGallery() {
        setModel { model ->
            model.copy(
                action = Action.FROM_GALLERY_PERMISSION,
                attachmentPanelVisible = false
            )
        }
    }

    fun fromGalleryAvailable() {
        setModel { model ->
            model.copy(
                action = Action.FROM_GALLERY,
                attachmentPanelVisible = false,
                cameraUri = null
            )
        }
    }

    fun fromStorage() {
        setModel { model ->
            model.copy(
                action = Action.FROM_STORAGE_PERMISSION,
                attachmentPanelVisible = false
            )
        }
    }

    fun fromStorageAvailable() {
        setModel { model ->
            model.copy(
                action = Action.FROM_STORAGE,
                attachmentPanelVisible = false,
                cameraUri = null
            )
        }
    }

    fun resetAction() {
        setModel { model ->
            model.copy(
                action = null,
                attachmentPanelVisible = false,
                cameraUri = null
            )
        }
    }

    fun actionCompleted(uriList: List<UsedeskFileInfo>) {
        setModel { model ->
            model.copy(
                messageDraft = model.messageDraft.copy(files = uriList.toSet().toList()),
                cameraUri = null,
                action = null,
                attachmentPanelVisible = false
            )
        }
        doIt(usedeskChat.setMessageDraftRx(modelLiveData.value.messageDraft))
    }

    override fun onCleared() {
        super.onCleared()

        UsedeskChatSdk.getInstance()
            ?.removeActionListener(actionListenerRx)

        UsedeskChatSdk.release(false)
    }

    data class Model(
        val messageDraft: UsedeskMessageDraft = UsedeskMessageDraft(),
        val fabToBottom: Boolean = false,
        val messages: List<UsedeskMessage> = listOf(),
        val messagesScroll: Long = 0L,
        val attachmentPanelVisible: Boolean = false,
        val action: Action? = null,
        val cameraUri: String? = null
    )

    enum class Action {
        FROM_CAMERA_PERMISSION,
        FROM_GALLERY_PERMISSION,
        FROM_STORAGE_PERMISSION,
        FROM_CAMERA,
        FROM_GALLERY,
        FROM_STORAGE
    }
}