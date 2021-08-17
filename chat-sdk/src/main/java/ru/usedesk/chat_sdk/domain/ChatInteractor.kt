package ru.usedesk.chat_sdk.domain

import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import ru.usedesk.chat_sdk.data.repository.api.IApiRepository
import ru.usedesk.chat_sdk.data.repository.api.entity.ChatInited
import ru.usedesk.chat_sdk.data.repository.configuration.IUserInfoRepository
import ru.usedesk.chat_sdk.data.repository.messages.IUsedeskMessagesRepository
import ru.usedesk.chat_sdk.entity.*
import ru.usedesk.common_sdk.entity.UsedeskEvent
import ru.usedesk.common_sdk.entity.UsedeskSingleLifeEvent
import ru.usedesk.common_sdk.entity.exceptions.UsedeskException
import ru.usedesk.common_sdk.utils.UsedeskRxUtil.safeCompletableIo
import ru.usedesk.common_sdk.utils.UsedeskRxUtil.safeSingleIo
import toothpick.InjectConstructor
import java.util.*
import java.util.concurrent.TimeUnit

@InjectConstructor
internal class ChatInteractor(
    private val configuration: UsedeskChatConfiguration,
    private val userInfoRepository: IUserInfoRepository,
    private val apiRepository: IApiRepository,
    private val messagesRepository: IUsedeskMessagesRepository,
    private val ioScheduler: Scheduler
) : IUsedeskChat {

    private var token: String? = null
    private var initClientMessage: String? = configuration.clientInitMessage
    private var initClientOfflineForm: String? = null

    private var actionListeners = mutableSetOf<IUsedeskActionListener>()
    private var actionListenersRx = mutableSetOf<IUsedeskActionListenerRx>()

    private val connectedStateSubject = BehaviorSubject.createDefault(false)
    private val clientTokenSubject = BehaviorSubject.create<String>()
    private val messagesSubject = BehaviorSubject.create<List<UsedeskMessage>>()
    private val messageSubject = BehaviorSubject.create<UsedeskMessage>()
    private val newMessageSubject = PublishSubject.create<UsedeskMessage>()
    private val messageUpdateSubject = PublishSubject.create<UsedeskMessage>()
    private val offlineFormExpectedSubject = BehaviorSubject.create<UsedeskOfflineFormSettings>()
    private val feedbackSubject = BehaviorSubject.create<UsedeskEvent<Any?>>()
    private val exceptionSubject = BehaviorSubject.create<Exception>()

    private var reconnectDisposable: Disposable? = null
    private val listenersDisposables = mutableListOf<Disposable>()

    private var lastMessages = listOf<UsedeskMessage>()

    private var localId = -1000L

    private var chatInited: ChatInited? = null
    private var offlineFormToChat = false

    init {
        listenersDisposables.apply {
            add(connectedStateSubject.subscribe {
                actionListeners.forEach { listener ->
                    listener.onConnectedState(it)
                }
            })

            add(messagesSubject.subscribe {
                actionListeners.forEach { listener ->
                    listener.onMessagesReceived(it)
                }
            })

            add(messageSubject.subscribe {
                actionListeners.forEach { listener ->
                    listener.onMessageReceived(it)
                }
            })

            add(newMessageSubject.subscribe {
                actionListeners.forEach { listener ->
                    listener.onNewMessageReceived(it)
                }
            })

            add(messageUpdateSubject.subscribe {
                actionListeners.forEach { listener ->
                    listener.onMessageUpdated(it)
                }
            })

            add(offlineFormExpectedSubject.subscribe {
                actionListeners.forEach { listener ->
                    listener.onOfflineFormExpected(it)
                }
            })

            add(feedbackSubject.subscribe {
                actionListeners.forEach { listener ->
                    listener.onFeedbackReceived()
                }
            })

            add(exceptionSubject.subscribe {
                actionListeners.forEach { listener ->
                    listener.onException(it)
                }
            })
        }
    }

    private val eventListener = object : IApiRepository.EventListener {
        override fun onConnected() {
            connectedStateSubject.onNext(true)
        }

        override fun onDisconnected() {
            if (reconnectDisposable?.isDisposed != false) {
                reconnectDisposable = Completable.timer(5, TimeUnit.SECONDS).subscribe {
                    try {
                        connect()
                    } catch (e: Exception) {
                        //nothing
                    }
                }
            }

            connectedStateSubject.onNext(false)
        }

        override fun onTokenError() {
            try {
                apiRepository.init(configuration, token)
            } catch (e: UsedeskException) {
                onException(e)
            }
        }

        override fun onFeedback() {
            feedbackSubject.onNext(UsedeskSingleLifeEvent(null))
        }

        override fun onException(exception: Exception) {
            exceptionSubject.onNext(exception)
        }

        override fun onChatInited(chatInited: ChatInited) {
            this@ChatInteractor.chatInited = chatInited
            this@ChatInteractor.onChatInited(chatInited)
        }

        override fun onMessagesReceived(newMessages: List<UsedeskMessage>) {
            this@ChatInteractor.onMessagesNew(newMessages, false)
        }

        override fun onMessageUpdated(message: UsedeskMessage) {
            this@ChatInteractor.onMessageUpdate(message)
        }

        override fun onOfflineForm(
            offlineFormSettings: UsedeskOfflineFormSettings,
            chatInited: ChatInited
        ) {
            this@ChatInteractor.chatInited = chatInited
            this@ChatInteractor.offlineFormToChat =
                offlineFormSettings.workType == UsedeskOfflineFormSettings.WorkType.ALWAYS_ENABLED_CALLBACK_WITH_CHAT
            offlineFormExpectedSubject.onNext(offlineFormSettings)
        }

        override fun onSetEmailSuccess() {
            sendInitMessage()
        }
    }

    private fun sendInitMessage() {
        val initMessage = initClientOfflineForm ?: initClientMessage
        initMessage?.let {
            try {
                send(it)
                initClientMessage = null
                initClientOfflineForm = null
            } catch (e: Exception) {
                //nothing
            }
        }
    }

    override fun connect() {
        reconnectDisposable?.dispose()
        reconnectDisposable = null
        token = if (!isStringEmpty(this.configuration.clientToken)) {
            this.configuration.clientToken
        } else {
            userInfoRepository.getConfigurationNullable(configuration)?.clientToken
        }
        apiRepository.connect(
            this.configuration.urlChat,
            token,
            this.configuration,
            eventListener
        )
    }

    private fun isStringEmpty(text: String?): Boolean {
        return text?.isEmpty() != false
    }

    private fun onMessageUpdate(message: UsedeskMessage) {
        lastMessages = lastMessages.map {
            if ((it is UsedeskMessageClient &&
                        message is UsedeskMessageClient &&
                        it.localId == message.localId)
                || (it is UsedeskMessageAgent &&
                        message is UsedeskMessageAgent &&
                        it.id == message.id
                        )
            ) {
                message
            } else {
                it
            }
        }
        messagesSubject.onNext(lastMessages)
        messageUpdateSubject.onNext(message)
    }

    private fun onMessagesNew(
        messages: List<UsedeskMessage>,
        isInited: Boolean
    ) {
        lastMessages = lastMessages + messages
        messages.forEach { message ->
            messageSubject.onNext(message)
            if (!isInited) {
                newMessageSubject.onNext(message)
            }
        }
        messagesSubject.onNext(lastMessages)
    }

    private fun runTimeout(message: UsedeskMessage) {
        val ignore = Completable.timer(SENDING_TIMEOUT_SECONDS, TimeUnit.SECONDS).subscribe {
            onMessageSendingFailed(message)
        }
    }

    override fun disconnect() {
        apiRepository.disconnect()
    }

    override fun addActionListener(
        listener: IUsedeskActionListener
    ) {
        actionListeners.add(listener)
    }

    override fun removeActionListener(listener: IUsedeskActionListener) {
        actionListeners.remove(listener)
    }

    override fun addActionListener(
        listener: IUsedeskActionListenerRx
    ) {
        actionListenersRx.add(listener)
        listener.onObservables(
            connectedStateSubject,
            clientTokenSubject,
            messageSubject,
            newMessageSubject,
            messagesSubject,
            messageUpdateSubject,
            offlineFormExpectedSubject,
            feedbackSubject,
            exceptionSubject
        )
    }

    override fun removeActionListener(listener: IUsedeskActionListenerRx) {
        actionListenersRx.remove(listener)
        listener.onDispose()
    }

    override fun isNoListeners(): Boolean {
        return actionListeners.isEmpty() && actionListenersRx.isEmpty()
    }

    override fun send(textMessage: String) {
        val message = textMessage.trim()
        if (message.isNotEmpty()) {
            val sendingMessage = createSendingMessage(message)
            eventListener.onMessagesReceived(listOf(sendingMessage))
            sendText(sendingMessage)
        }
    }

    private fun sendText(sendingMessage: UsedeskMessageText) {
        runTimeout(sendingMessage)

        try {
            apiRepository.send(sendingMessage)
        } catch (e: Exception) {
            onMessageSendingFailed(sendingMessage)
            throw e
        }
    }

    override fun send(usedeskFileInfoList: List<UsedeskFileInfo>) {
        var exc: Exception? = null
        val sendingFiles = usedeskFileInfoList.map { usedeskFileInfo ->
            createSendingMessage(usedeskFileInfo).also {
                eventListener.onMessagesReceived(listOf(it))
            }
        }

        sendingFiles.forEach { sendingMessage ->
            try {
                sendFile(sendingMessage)
            } catch (e: Exception) {
                exc = e
            }
        }

        exc?.let {
            throw it
        }
    }

    private fun sendFile(sendingMessage: UsedeskMessageFile) {
        token?.also { token ->
            try {
                apiRepository.send(configuration, token, sendingMessage)
                runTimeout(sendingMessage)
            } catch (e: Exception) {
                onMessageSendingFailed(sendingMessage)
                throw e
            }
        }
    }

    private fun onMessageSendingFailed(sendingMessage: UsedeskMessage) {
        val failedMessage = lastMessages.firstOrNull {
            it.id == sendingMessage.id
        }
        if (failedMessage is UsedeskMessageClient &&
            failedMessage.status != UsedeskMessageClient.Status.SUCCESSFULLY_SENT
        ) {
            when (failedMessage) {
                is UsedeskMessageClientText -> {
                    UsedeskMessageClientText(
                        failedMessage.id,
                        failedMessage.createdAt,
                        failedMessage.text,
                        UsedeskMessageClient.Status.SEND_FAILED
                    )
                }
                is UsedeskMessageClientFile -> {
                    UsedeskMessageClientFile(
                        failedMessage.id,
                        failedMessage.createdAt,
                        failedMessage.file,
                        UsedeskMessageClient.Status.SEND_FAILED
                    )
                }
                is UsedeskMessageClientImage -> {
                    UsedeskMessageClientImage(
                        failedMessage.id,
                        failedMessage.createdAt,
                        failedMessage.file,
                        UsedeskMessageClient.Status.SEND_FAILED
                    )
                }
                else -> null
            }?.let {
                onMessageUpdate(it)
            }
        }
    }

    override fun send(agentMessage: UsedeskMessageAgentText, feedback: UsedeskFeedback) {
        apiRepository.send(agentMessage.id, feedback)

        onMessageUpdate(
            UsedeskMessageAgentText(
                agentMessage.id,
                agentMessage.createdAt,
                agentMessage.text,
                agentMessage.buttons,
                false,
                feedback,
                agentMessage.name,
                agentMessage.avatar
            )
        )
    }

    override fun send(offlineForm: UsedeskOfflineForm) {
        if (offlineFormToChat) {
            offlineForm.run {
                val fields = offlineForm.fields.filter { field ->
                    field.value.isNotEmpty()
                }.map { field ->
                    field.value
                }
                initClientOfflineForm = (listOf(clientName, clientEmail, topic)
                        + fields
                        + offlineForm.message)
                    .joinToString(separator = "\n")
                chatInited?.let { onChatInited(it) }
            }
        } else {
            apiRepository.send(configuration, configuration.getCompanyAndChannel(), offlineForm)
        }
    }

    override fun sendAgain(id: Long) {
        val message = lastMessages.firstOrNull {
            it.id == id
        }
        if (message is UsedeskMessageClient
            && message.status == UsedeskMessageClient.Status.SEND_FAILED
        ) {
            when (message) {
                is UsedeskMessageClientText -> {
                    val sendingMessage = UsedeskMessageClientText(
                        message.id,
                        message.createdAt,
                        message.text,
                        UsedeskMessageClient.Status.SENDING
                    )
                    onMessageUpdate(sendingMessage)
                    sendText(sendingMessage)
                }
                is UsedeskMessageClientImage -> {
                    val sendingMessage = UsedeskMessageClientImage(
                        message.id,
                        message.createdAt,
                        message.file,
                        UsedeskMessageClient.Status.SENDING
                    )
                    onMessageUpdate(sendingMessage)
                    sendFile(sendingMessage)
                }
                is UsedeskMessageClientFile -> {
                    val sendingMessage = UsedeskMessageClientFile(
                        message.id,
                        message.createdAt,
                        message.file,
                        UsedeskMessageClient.Status.SENDING
                    )
                    onMessageUpdate(sendingMessage)
                    sendFile(sendingMessage)
                }
            }
        }
    }

    override fun setMessageDraft(messageDraft: UsedeskMessageDraft) {
        messagesRepository.setDraft(messageDraft)
    }

    override fun setMessageDraftRx(messageDraft: UsedeskMessageDraft): Completable {
        return safeCompletableIo(ioScheduler) {
            setMessageDraft(messageDraft)
        }
    }

    override fun getMessageDraft(): UsedeskMessageDraft {
        return messagesRepository.getDraft() ?: UsedeskMessageDraft()
    }

    override fun getMessageDraftRx(): Single<UsedeskMessageDraft> {
        return safeSingleIo(ioScheduler) {
            getMessageDraft()
        }
    }

    private fun createSendingMessage(text: String): UsedeskMessageText {
        localId--
        val calendar = Calendar.getInstance()
        return UsedeskMessageClientText(
            localId,
            calendar,
            text,
            UsedeskMessageClient.Status.SENDING
        )
    }

    private fun createSendingMessage(fileInfo: UsedeskFileInfo): UsedeskMessageFile {
        localId--
        val calendar = Calendar.getInstance()
        val file = UsedeskFile.create(
            fileInfo.uri.toString(),
            fileInfo.type,
            "",
            fileInfo.name
        )
        return if (fileInfo.isImage()) {
            UsedeskMessageClientImage(localId, calendar, file, UsedeskMessageClient.Status.SENDING)
        } else {
            UsedeskMessageClientFile(localId, calendar, file, UsedeskMessageClient.Status.SENDING)
        }
    }

    override fun connectRx(): Completable {
        return safeCompletableIo(ioScheduler) {
            connect()
        }
    }

    override fun sendRx(textMessage: String): Completable {
        return safeCompletableIo(ioScheduler) {
            send(textMessage)
        }
    }

    override fun sendRx(usedeskFileInfoList: List<UsedeskFileInfo>): Completable {
        return safeCompletableIo(ioScheduler) {
            send(usedeskFileInfoList)
        }
    }

    override fun sendRx(
        agentMessage: UsedeskMessageAgentText,
        feedback: UsedeskFeedback
    ): Completable {
        return safeCompletableIo(ioScheduler) {
            send(agentMessage, feedback)
        }
    }

    override fun sendRx(offlineForm: UsedeskOfflineForm): Completable {
        return safeCompletableIo(ioScheduler) {
            send(offlineForm)
        }
    }

    override fun sendAgainRx(id: Long): Completable {
        return safeCompletableIo(ioScheduler) {
            sendAgain(id)
        }
    }

    override fun disconnectRx(): Completable {
        return safeCompletableIo(ioScheduler) {
            disconnect()
        }
    }

    override fun release() {
        disconnect()
        listenersDisposables.forEach {
            it.dispose()
        }
    }

    override fun releaseRx(): Completable {
        return safeCompletableIo(ioScheduler) {
            listenersDisposables.forEach {
                it.dispose()
            }
            disconnect()
        }
    }

    private fun sendUserEmail() {
        try {
            apiRepository.send(
                token,
                configuration.clientEmail,
                configuration.clientName,
                configuration.clientNote,
                configuration.clientPhoneNumber,
                configuration.clientAdditionalId
            )
        } catch (e: UsedeskException) {
            exceptionSubject.onNext(e)
        }
    }

    private fun onChatInited(chatInited: ChatInited) {
        this.token = chatInited.token
        clientTokenSubject.onNext(chatInited.token)

        val ids = lastMessages.map {
            it.id
        }
        val filteredMessages = chatInited.messages.filter {
            it.id !in ids
        }
        onMessagesNew(filteredMessages, true)

        if (userInfoRepository.getConfigurationNullable(configuration)?.clientInitMessage?.equals(
                initClientMessage
            ) == true ||
            initClientOfflineForm != null
        ) {
            initClientMessage = null
        }
        userInfoRepository.setConfiguration(configuration.copy(clientToken = token))

        if (chatInited.waitingEmail) {
            sendUserEmail()
        } else {
            eventListener.onSetEmailSuccess()
        }
    }

    companion object {
        private const val SENDING_TIMEOUT_SECONDS = 60L
    }
}